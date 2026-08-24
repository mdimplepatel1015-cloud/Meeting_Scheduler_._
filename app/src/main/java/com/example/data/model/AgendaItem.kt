package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "agenda_items")
data class AgendaItem(
    @PrimaryKey val id: String,
    val meetingId: String,
    val orderIndex: Int,
    val title: String,
    val durationMinutes: Int = 10,
    val presenter: String = "All",
    val description: String = "",
    val isCompleted: Boolean = false
)
