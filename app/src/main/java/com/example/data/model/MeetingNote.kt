package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meeting_notes")
data class MeetingNote(
    @PrimaryKey val id: String,
    val meetingId: String,
    val authorName: String,
    val text: String,
    val isPrivate: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
