package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "decisions")
data class Decision(
    @PrimaryKey val id: String,
    val meetingId: String,
    val meetingTitle: String = "Architecture Review",
    val title: String,
    val ownerName: String,
    val date: String, // e.g. "Aug 23, 2026"
    val relatedProject: String = "Core Platform v3",
    val contextNotes: String = "",
    val status: String = "Approved", // Approved, Pending Validation, Superseded
    val impactLevel: String = "High", // High, Medium, Low
    val createdAt: Long = System.currentTimeMillis()
)
