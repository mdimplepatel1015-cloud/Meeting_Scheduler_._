package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transcript_lines")
data class TranscriptLine(
    @PrimaryKey val id: String,
    val meetingId: String,
    val speaker: String,
    val timeLabel: String, // e.g. "04:12"
    val text: String,
    val isDecision: Boolean = false,
    val isActionItem: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
