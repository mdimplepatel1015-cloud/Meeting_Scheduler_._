package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meeting_templates")
data class MeetingTemplate(
    @PrimaryKey val id: String,
    val title: String,
    val description: String,
    val type: MeetingType,
    val defaultDurationMinutes: Int = 30,
    val category: String = "General",
    val defaultAgendaJson: String = "", // Comma-delimited or JSON string of agenda topics
    val defaultPriority: MeetingPriority = MeetingPriority.MEDIUM,
    val defaultRecurrence: MeetingRecurrence = MeetingRecurrence.NONE,
    val iconName: String = "group"
)
