package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class NotificationType {
    INVITATION,
    STARTING_SOON,
    RESCHEDULED,
    CANCELLED,
    RSVP_UPDATE,
    ACTION_ASSIGNED,
    ACTION_OVERDUE,
    SUMMARY_READY,
    AI_INSIGHT
}

@Entity(tableName = "notifications")
data class NotificationItem(
    @PrimaryKey val id: String,
    val title: String,
    val message: String,
    val type: NotificationType = NotificationType.INVITATION,
    val timestamp: Long = System.currentTimeMillis(),
    val timeFormatted: String = "10m ago",
    val isRead: Boolean = false,
    val relatedMeetingId: String? = null,
    val actionPayload: String? = null
)
