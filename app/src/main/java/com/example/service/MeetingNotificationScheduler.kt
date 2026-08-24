package com.example.service

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.example.data.local.AppDatabase
import com.example.data.model.Meeting
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object MeetingNotificationScheduler {
    private const val TAG = "MeetingNotificationScheduler"

    fun scheduleMeetingReminder(context: Context, meeting: Meeting) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return

            val triggerTimestamp = calculateTriggerTime(meeting.date, meeting.startTime, meeting.reminderMinutesBefore)
            val now = System.currentTimeMillis()

            if (triggerTimestamp <= now) {
                Log.d(TAG, "Meeting ${meeting.title} reminder time is already in the past, skipping exact alarm.")
                return
            }

            val intent = Intent(context, MeetingAlarmReceiver::class.java).apply {
                action = MeetingAlarmReceiver.ACTION_MEETING_REMINDER
                putExtra(MeetingAlarmReceiver.EXTRA_MEETING_ID, meeting.id)
                putExtra(MeetingAlarmReceiver.EXTRA_MEETING_TITLE, meeting.title)
                putExtra(MeetingAlarmReceiver.EXTRA_MEETING_TIME, "${meeting.date} at ${meeting.startTime}")
                putExtra(MeetingAlarmReceiver.EXTRA_MEETING_LOCATION, meeting.location)
                putExtra(MeetingAlarmReceiver.EXTRA_MEETING_LINK, meeting.meetingLink)
                putExtra(MeetingAlarmReceiver.EXTRA_REMINDER_MINUTES, meeting.reminderMinutesBefore)
                putExtra(MeetingAlarmReceiver.EXTRA_MEETING_TAGS, meeting.tags.joinToString(", "))
            }

            val requestCode = meeting.id.hashCode()
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                requestCode,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimestamp, pendingIntent)
                } else {
                    alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimestamp, pendingIntent)
                }
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerTimestamp, pendingIntent)
            }

            Log.i(TAG, "Scheduled reminder for '${meeting.title}' at timestamp $triggerTimestamp (${(triggerTimestamp - now) / 60000} minutes from now)")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to schedule reminder for ${meeting.id}", e)
        }
    }

    fun cancelMeetingReminder(context: Context, meetingId: String) {
        try {
            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
            val intent = Intent(context, MeetingAlarmReceiver::class.java).apply {
                action = MeetingAlarmReceiver.ACTION_MEETING_REMINDER
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                meetingId.hashCode(),
                intent,
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
            )
            if (pendingIntent != null) {
                alarmManager.cancel(pendingIntent)
                pendingIntent.cancel()
                Log.d(TAG, "Cancelled reminder for meeting $meetingId")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to cancel reminder for $meetingId", e)
        }
    }

    fun scheduleAllUpcomingMeetingsFromDatabase(context: Context) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val upcomingMeetings = db.meetingDao().getUpcomingMeetingsListSync()
                Log.i(TAG, "Scheduling reminders for ${upcomingMeetings.size} upcoming meetings from Room database.")
                upcomingMeetings.forEach { meeting ->
                    scheduleMeetingReminder(context, meeting)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to schedule upcoming meetings from Room database", e)
            }
        }
    }

    fun sendTimeNearlyUpNotification(context: Context, meeting: Meeting, remainingSeconds: Long) {
        try {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? android.app.NotificationManager ?: return
            val channelId = "meetiq_live_meeting_timer_channel"

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = android.app.NotificationChannel(
                    channelId,
                    "Live Meeting Time Alerts",
                    android.app.NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Alerts when an active meeting is nearly out of scheduled time"
                    enableVibration(true)
                }
                notificationManager.createNotificationChannel(channel)
            }

            val remainingText = if (remainingSeconds <= 60) "Less than 1 minute remaining" else "${(remainingSeconds + 59) / 60} minutes remaining"

            val notification = androidx.core.app.NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
                .setContentTitle("⏱️ Time Nearly Up: ${meeting.title}")
                .setContentText("$remainingText in scheduled session (${meeting.durationMinutes}m total). Wrap up key action items.")
                .setStyle(androidx.core.app.NotificationCompat.BigTextStyle()
                    .bigText("Active meeting '${meeting.title}' is reaching its scheduled end time. Only $remainingText left. Consider concluding discussion points and recording final action items."))
                .setPriority(androidx.core.app.NotificationCompat.PRIORITY_HIGH)
                .setDefaults(androidx.core.app.NotificationCompat.DEFAULT_ALL)
                .setAutoCancel(true)
                .build()

            notificationManager.notify((meeting.id + "_time_warn").hashCode(), notification)
            Log.i(TAG, "Sent time warning notification for meeting ${meeting.title}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to post time warning notification", e)
        }
    }

    private fun calculateTriggerTime(dateStr: String, startTimeStr: String, minutesBefore: Int): Long {
        return try {
            val format = SimpleDateFormat("yyyy-MM-dd hh:mm a", Locale.US)
            val fullDateStr = "$dateStr $startTimeStr"
            val parsedDate = format.parse(fullDateStr)
            val calendar = Calendar.getInstance()
            if (parsedDate != null) {
                calendar.time = parsedDate
                calendar.add(Calendar.MINUTE, -minutesBefore)
                calendar.timeInMillis
            } else {
                System.currentTimeMillis() + (15 * 60 * 1000)
            }
        } catch (e: Exception) {
            // Fallback: 10 minutes in future
            System.currentTimeMillis() + (10 * 60 * 1000)
        }
    }
}
