package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessage(
    @PrimaryKey val id: String,
    val meetingId: String,
    val senderName: String,
    val text: String,
    val timestamp: String,
    val isAiAssistant: Boolean = false,
    val isSystem: Boolean = false
)
