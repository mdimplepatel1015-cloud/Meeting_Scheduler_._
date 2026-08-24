package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class RsvpStatus {
    ACCEPTED,
    PENDING,
    DECLINED,
    MAYBE
}

@Entity(tableName = "participants")
data class Participant(
    @PrimaryKey val id: String,
    val meetingId: String,
    val name: String,
    val email: String,
    val role: String = "Attendee", // Organizer, Presenter, Attendee, Optional
    val rsvpStatus: RsvpStatus = RsvpStatus.ACCEPTED,
    val isOrganizer: Boolean = false,
    val isOptional: Boolean = false,
    val avatarColor: String = "#4F46E5",
    val speakingTimeSeconds: Int = 0,
    val speakingPercentage: Int = 0,
    val isSpeakingLive: Boolean = false,
    val micMuted: Boolean = false,
    val videoActive: Boolean = true
)
