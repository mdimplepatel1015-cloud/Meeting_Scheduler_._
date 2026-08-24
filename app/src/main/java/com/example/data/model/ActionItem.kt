package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class ActionItemStatus {
    NOT_STARTED,
    IN_PROGRESS,
    COMPLETED,
    OVERDUE
}

@Entity(tableName = "action_items")
data class ActionItem(
    @PrimaryKey val id: String,
    val meetingId: String,
    val meetingTitle: String = "Sprint Sync",
    val title: String,
    val description: String = "",
    val assigneeName: String,
    val assigneeEmail: String = "",
    val priority: MeetingPriority = MeetingPriority.HIGH,
    val dueDate: String, // e.g. "Aug 28, 2026"
    val status: ActionItemStatus = ActionItemStatus.IN_PROGRESS,
    val createdAt: Long = System.currentTimeMillis()
)
