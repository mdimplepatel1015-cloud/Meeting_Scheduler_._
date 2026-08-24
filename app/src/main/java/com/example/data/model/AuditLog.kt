package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audit_logs")
data class AuditLog(
    @PrimaryKey val id: String,
    val action: String,
    val actorName: String,
    val actorRole: String,
    val timestamp: String,
    val details: String,
    val category: String = "Security" // Security, Meeting, Integration, AI
)
