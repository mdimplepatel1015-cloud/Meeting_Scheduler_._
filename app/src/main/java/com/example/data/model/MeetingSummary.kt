package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meeting_summaries")
data class MeetingSummary(
    @PrimaryKey val id: String,
    val meetingId: String,
    val objective: String,
    val keyDiscussionPoints: String, // Bullet points or markdown
    val decisionsSummary: String,
    val actionItemsSummary: String,
    val nextMeetingDate: String? = null,
    val generatedAt: String = "Just now",
    val sentimentScore: String = "Highly Productive (94% Alignment)",
    val riskFactors: String = "API third-party latency risk flagged by engineering team."
)
