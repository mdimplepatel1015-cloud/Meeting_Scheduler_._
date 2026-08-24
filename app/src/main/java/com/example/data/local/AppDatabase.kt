package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.ActionItem
import com.example.data.model.AgendaItem
import com.example.data.model.AuditLog
import com.example.data.model.ChatMessage
import com.example.data.model.Decision
import com.example.data.model.Meeting
import com.example.data.model.MeetingNote
import com.example.data.model.MeetingSummary
import com.example.data.model.MeetingTemplate
import com.example.data.model.NotificationItem
import com.example.data.model.Participant
import com.example.data.model.TranscriptLine
import com.example.data.model.User

@Database(
    entities = [
        User::class,
        Meeting::class,
        Participant::class,
        AgendaItem::class,
        MeetingNote::class,
        TranscriptLine::class,
        MeetingSummary::class,
        ActionItem::class,
        Decision::class,
        NotificationItem::class,
        MeetingTemplate::class,
        AuditLog::class,
        ChatMessage::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun meetingDao(): MeetingDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "meetiq_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
