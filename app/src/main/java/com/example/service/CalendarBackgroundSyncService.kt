package com.example.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.data.local.AppDatabase
import com.example.data.repository.MeetingRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class CalendarBackgroundSyncService : Service() {

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "CalendarBackgroundSyncService started for background synchronization.")

        serviceScope.launch {
            try {
                val db = AppDatabase.getDatabase(applicationContext)
                val repository = MeetingRepository(db.meetingDao(), db.userDao())
                val importedCount = CalendarSyncHelper.syncCalendarWithDatabase(applicationContext, repository)
                Log.d(TAG, "Background calendar sync completed. Imported $importedCount meetings.")

                if (importedCount > 0) {
                    showSyncCompletedNotification(importedCount)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Background sync failed", e)
            } finally {
                stopSelf(startId)
            }
        }

        return START_NOT_STICKY
    }

    private fun showSyncCompletedNotification(count: Int) {
        val channelId = "meetiq_calendar_sync_channel"
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Calendar Sync Notifications",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifies when device calendar meetings are synchronized into MeetIQ"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_menu_today)
            .setContentTitle("Calendar Synchronization Complete")
            .setContentText("Imported $count calendar events into MeetIQ meetings.")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setAutoCancel(true)
            .build()

        notificationManager.notify(7788, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        private const val TAG = "CalendarBgSyncService"

        fun startSync(context: Context) {
            val intent = Intent(context, CalendarBackgroundSyncService::class.java)
            context.startService(intent)
        }
    }
}
