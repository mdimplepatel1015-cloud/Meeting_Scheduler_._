package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class MeetingType {
    TEAM_MEETING,
    CLIENT_MEETING,
    ONE_ON_ONE,
    INTERVIEW,
    SALES_MEETING,
    PROJECT_MEETING,
    REVIEW_MEETING,
    TRAINING,
    PRESENTATION,
    BRAINSTORMING,
    OTHER
}

enum class MeetingPriority {
    HIGH,
    MEDIUM,
    LOW
}

enum class MeetingVisibility {
    PUBLIC,
    PRIVATE,
    CONFIDENTIAL
}

enum class MeetingRecurrence {
    NONE,
    DAILY,
    WEEKLY,
    BIWEEKLY,
    MONTHLY
}

enum class MeetingStatus {
    UPCOMING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED,
    RESCHEDULED
}

enum class CalendarEventType {
    MEETING,
    EVENT,
    DEADLINE,
    TASK,
    PERSONAL
}

@Entity(tableName = "meetings")
data class Meeting(
    @PrimaryKey val id: String,
    val title: String,
    val description: String = "",
    val type: MeetingType = MeetingType.TEAM_MEETING,
    val eventType: CalendarEventType = CalendarEventType.MEETING,
    val category: String = "Engineering",
    val date: String, // e.g. "2026-08-23" (YYYY-MM-DD)
    val startTime: String, // e.g. "10:00 AM"
    val endTime: String, // e.g. "10:45 AM"
    val startTimestamp: Long = 0L,
    val durationMinutes: Int = 45,
    val timezone: String = "PST (UTC-7)",
    val location: String = "Google Meet",
    val meetingLink: String = "https://meet.google.com/xyz-meetiq-ai",
    val priority: MeetingPriority = MeetingPriority.HIGH,
    val reminderMinutesBefore: Int = 15,
    val recurrence: MeetingRecurrence = MeetingRecurrence.NONE,
    val visibility: MeetingVisibility = MeetingVisibility.PRIVATE,
    val status: MeetingStatus = MeetingStatus.UPCOMING,
    val healthScore: Int = 88,
    val healthScoreExplanation: String = "Clear agenda, strong action item capture, balanced discussion.",
    val isRecurring: Boolean = false,
    val recurringInsight: String? = null,
    val preparationBriefing: String? = null,
    val recordingActive: Boolean = false,
    val hasTranscript: Boolean = false,
    val hasSummary: Boolean = false,
    val tags: List<String> = listOf("Work")
)
