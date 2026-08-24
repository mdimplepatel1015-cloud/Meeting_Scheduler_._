package com.example.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R

class MeetingAlarmReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_MEETING_REMINDER = "com.example.meetiq.ACTION_MEETING_REMINDER"
        const val CHANNEL_ID = "meetiq_meeting_reminders_channel"
        const val CHANNEL_NAME = "Meeting Reminders"

        const val EXTRA_MEETING_ID = "extra_meeting_id"
        const val EXTRA_MEETING_TITLE = "extra_meeting_title"
        const val EXTRA_MEETING_TIME = "extra_meeting_time"
        const val EXTRA_MEETING_LOCATION = "extra_meeting_location"
        const val EXTRA_MEETING_LINK = "extra_meeting_link"
        const val EXTRA_REMINDER_MINUTES = "extra_reminder_minutes"
        const val EXTRA_MEETING_TAGS = "extra_meeting_tags"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        Log.d("MeetingAlarmReceiver", "Received intent action: $action")

        if (action == Intent.ACTION_BOOT_COMPLETED) {
            // Re-schedule all upcoming meetings after device reboot
            MeetingNotificationScheduler.scheduleAllUpcomingMeetingsFromDatabase(context)
            return
        }

        if (action == ACTION_MEETING_REMINDER) {
            val meetingId = intent.getStringExtra(EXTRA_MEETING_ID) ?: return
            val title = intent.getStringExtra(EXTRA_MEETING_TITLE) ?: "Upcoming Meeting"
            val time = intent.getStringExtra(EXTRA_MEETING_TIME) ?: "Starting soon"
            val location = intent.getStringExtra(EXTRA_MEETING_LOCATION) ?: "Virtual"
            val link = intent.getStringExtra(EXTRA_MEETING_LINK) ?: ""
            val reminderMinutes = intent.getIntExtra(EXTRA_REMINDER_MINUTES, 15)
            val tags = intent.getStringExtra(EXTRA_MEETING_TAGS) ?: ""

            createNotificationChannel(context)

            // Content intent to open app
            val openAppIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra("NAVIGATE_MEETING_ID", meetingId)
            }
            val contentPendingIntent = PendingIntent.getActivity(
                context,
                meetingId.hashCode(),
                openAppIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val tagInfo = if (tags.isNotBlank()) " • [$tags]" else ""
            val bigTextMessage = "Starting in $reminderMinutes minutes at $time.\nLocation: $location$tagInfo\nTap to open agenda, AI briefing & live room."

            val notificationBuilder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle("⏰ Reminder: $title")
                .setContentText("Starts in $reminderMinutes mins at $time ($location)")
                .setStyle(NotificationCompat.BigTextStyle().bigText(bigTextMessage))
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setAutoCancel(true)
                .setContentIntent(contentPendingIntent)

            // Add action to open meeting link if present
            if (link.isNotBlank() && (link.startsWith("http://") || link.startsWith("https://"))) {
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(link)).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                val linkPendingIntent = PendingIntent.getActivity(
                    context,
                    meetingId.hashCode() + 1,
                    browserIntent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
                notificationBuilder.addAction(
                    android.R.drawable.ic_menu_send,
                    "Join Call",
                    linkPendingIntent
                )
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            val notificationId = meetingId.hashCode()
            notificationManager?.notify(notificationId, notificationBuilder.build())
            Log.i("MeetingAlarmReceiver", "Notification posted for meeting $title (ID: $meetingId)")
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
            if (notificationManager?.getNotificationChannel(CHANNEL_ID) == null) {
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications and reminders for scheduled meetings"
                    enableVibration(true)
                    enableLights(true)
                }
                notificationManager?.createNotificationChannel(channel)
            }
        }
    }
}
