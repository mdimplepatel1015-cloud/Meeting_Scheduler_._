package com.example.data.model

data class HealthScoreBreakdown(
    val overallScore: Int = 86,
    val agendaClarityScore: Int = 90,
    val timeEfficiencyScore: Int = 82,
    val actionItemCompletionScore: Int = 88,
    val participationBalanceScore: Int = 76,
    val decisionClarityScore: Int = 85,
    val primaryObservation: String = "Strong agenda pacing. 2 action items captured with assigned owners. Moderate speaking imbalance noted between Rahul and Alex.",
    val recommendation: String = "Consider reducing weekly sprint cadence by 15 minutes to improve time efficiency."
)

data class ProductivityMetrics(
    val totalMeetingsThisWeek: Int = 14,
    val totalMeetingHoursThisWeek: Float = 8.5f,
    val averageMeetingDurationMinutes: Int = 36,
    val actionItemCompletionRate: Int = 89,
    val meetingsAttended: Int = 12,
    val meetingsOrganized: Int = 4,
    val cancelledCount: Int = 1,
    val rescheduledCount: Int = 2,
    val busiestDay: String = "Wednesday (4.2 hrs)",
    val peakHours: String = "2:00 PM – 4:30 PM",
    val recurringMeetingsCount: Int = 6,
    val weeklyHoursDeltaPercent: Int = 18, // +18% compared to last week
    val recurringMeetingSuggestions: List<RecurringOptimizationSuggestion> = listOf(
        RecurringOptimizationSuggestion(
            meetingTitle = "Weekly Architecture Alignment",
            currentDuration = "60 min",
            suggestedDuration = "30 min",
            reason = "Meeting frequently finishes 25 min early with high agenda completion.",
            savingsHoursPerMonth = "2.0 hrs/month"
        ),
        RecurringOptimizationSuggestion(
            meetingTitle = "Bi-weekly Cross-team Info Sync",
            currentDuration = "45 min",
            suggestedDuration = "Async Status",
            reason = "Zero action items generated in past 3 sessions. Better suited as async document update.",
            savingsHoursPerMonth = "1.5 hrs/month"
        )
    )
)

data class RecurringOptimizationSuggestion(
    val meetingTitle: String,
    val currentDuration: String,
    val suggestedDuration: String,
    val reason: String,
    val savingsHoursPerMonth: String
)

data class SmartTimeSlot(
    val dayOfWeek: String, // e.g. "Tuesday"
    val date: String, // e.g. "Aug 25, 2026"
    val timeRange: String, // e.g. "11:00 AM – 11:45 AM"
    val matchScorePercent: Int, // e.g. 98
    val reason: String, // e.g. "All 6 participants available with 15m buffer"
    val hasConflict: Boolean = false,
    val conflictDetails: String? = null
)
