package com.example.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.ActionItem
import com.example.data.model.Decision
import com.example.data.model.HealthScoreBreakdown
import com.example.data.model.Meeting
import com.example.data.model.ProductivityMetrics
import java.io.File
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AnalyticsCsvExporter {

    fun generateAnalyticsCsvString(
        metrics: ProductivityMetrics,
        healthScore: HealthScoreBreakdown,
        meetings: List<Meeting>,
        actionItems: List<ActionItem>,
        decisions: List<Decision>
    ): String {
        val sb = StringBuilder()

        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())

        // Header Metadata
        sb.appendLine("sep=,") // Helpful hint for Microsoft Excel & Google Sheets delimiter
        sb.appendLine("# MeetIQ Meeting Intelligence & Productivity Analytics Export")
        sb.appendLine("# Generated At: $timestamp")
        sb.appendLine()

        // 1. EXECUTIVE PRODUCTIVITY SUMMARY METRICS
        sb.appendLine("=== EXECUTIVE PRODUCTIVITY SUMMARY ===")
        sb.appendLine("Metric,Value,Unit/Notes")
        sb.appendLine(csvRow("Total Meetings Tracked", metrics.totalMeetingsThisWeek, "sessions"))
        sb.appendLine(csvRow("Total Meeting Time", metrics.totalMeetingHoursThisWeek, "hours"))
        sb.appendLine(csvRow("Average Meeting Duration", metrics.averageMeetingDurationMinutes, "minutes"))
        sb.appendLine(csvRow("Action Item Completion Rate", "${metrics.actionItemCompletionRate}%", "follow-through index"))
        sb.appendLine(csvRow("Meetings Attended", metrics.meetingsAttended, "sessions"))
        sb.appendLine(csvRow("Meetings Organized", metrics.meetingsOrganized, "sessions"))
        sb.appendLine(csvRow("Cancelled / Rescheduled", "${metrics.cancelledCount} cancelled, ${metrics.rescheduledCount} rescheduled", "calendar agility"))
        sb.appendLine(csvRow("Busiest Day", metrics.busiestDay, "peak load"))
        sb.appendLine(csvRow("Peak Density Hours", metrics.peakHours, "focus conflict risk"))
        sb.appendLine(csvRow("Weekly Workload Delta", "+${metrics.weeklyHoursDeltaPercent}%", "vs prior week"))
        sb.appendLine(csvRow("Overall Health Score", "${healthScore.overallScore}/100", healthScore.primaryObservation))
        sb.appendLine(csvRow("Agenda Clarity Score", "${healthScore.agendaClarityScore}/100", "Clear objectives & topics"))
        sb.appendLine(csvRow("Time Efficiency Score", "${healthScore.timeEfficiencyScore}/100", "On-time starts & finishes"))
        sb.appendLine(csvRow("Participation Balance Score", "${healthScore.participationBalanceScore}/100", "Cross-speaker engagement"))
        sb.appendLine(csvRow("Decision Clarity Score", "${healthScore.decisionClarityScore}/100", "Concrete conclusions captured"))
        sb.appendLine()

        // 2. DETAILED MEETINGS BREAKDOWN
        sb.appendLine("=== DETAILED MEETING LOGS ===")
        sb.appendLine("Meeting ID,Title,Date,Start Time,End Time,Duration (Mins),Status,Category,Priority,Recurrence,Location,Health Score,Tags,Action Items Count,Decisions Count")
        meetings.forEach { m ->
            val mActionCount = actionItems.count { it.meetingId == m.id }
            val mDecisionCount = decisions.count { it.meetingId == m.id }
            val tagsFormatted = m.tags.joinToString("; ")

            sb.appendLine(
                csvRow(
                    m.id,
                    m.title,
                    m.date,
                    m.startTime,
                    m.endTime,
                    m.durationMinutes,
                    m.status.name,
                    m.category,
                    m.priority.name,
                    m.recurrence.name,
                    m.location,
                    "${m.healthScore}%",
                    tagsFormatted,
                    mActionCount,
                    mDecisionCount
                )
            )
        }
        sb.appendLine()

        // 3. ACTION ITEMS & DELIVERABLES BREAKDOWN
        sb.appendLine("=== ACTION ITEMS & DELIVERABLES ===")
        sb.appendLine("Item ID,Meeting ID,Meeting Title,Task Title,Assignee,Due Date,Priority,Status")
        actionItems.forEach { item ->
            val parentMeeting = meetings.find { it.id == item.meetingId }
            sb.appendLine(
                csvRow(
                    item.id,
                    item.meetingId,
                    parentMeeting?.title ?: "Unknown Meeting",
                    item.title,
                    item.assigneeName,
                    item.dueDate,
                    item.priority.name,
                    item.status.name
                )
            )
        }
        sb.appendLine()

        // 4. KEY DECISIONS LOG
        sb.appendLine("=== RECORDED DECISIONS ===")
        sb.appendLine("Decision ID,Meeting ID,Meeting Title,Decision Title,Context Notes,Impact Level,Owner")
        decisions.forEach { dec ->
            val parentMeeting = meetings.find { it.id == dec.meetingId }
            sb.appendLine(
                csvRow(
                    dec.id,
                    dec.meetingId,
                    parentMeeting?.title ?: "Unknown Meeting",
                    dec.title,
                    dec.contextNotes,
                    dec.impactLevel,
                    dec.ownerName
                )
            )
        }
        sb.appendLine()

        // 5. RECURRING OPTIMIZATION RECOMMENDATIONS
        sb.appendLine("=== AI RECURRING MEETING OPTIMIZATION RECOMMENDATIONS ===")
        sb.appendLine("Meeting Title,Current Duration,Suggested Duration,Potential Monthly Savings,Reasoning")
        metrics.recurringMeetingSuggestions.forEach { opt ->
            sb.appendLine(
                csvRow(
                    opt.meetingTitle,
                    opt.currentDuration,
                    opt.suggestedDuration,
                    opt.savingsHoursPerMonth,
                    opt.reason
                )
            )
        }

        return sb.toString()
    }

    fun exportAndShareAnalyticsCsv(
        context: Context,
        metrics: ProductivityMetrics,
        healthScore: HealthScoreBreakdown,
        meetings: List<Meeting>,
        actionItems: List<ActionItem>,
        decisions: List<Decision>
    ) {
        try {
            val csvContent = generateAnalyticsCsvString(metrics, healthScore, meetings, actionItems, decisions)
            val cacheDir = context.cacheDir
            val dateStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
            val fileName = "meetiq_analytics_$dateStamp.csv"
            val csvFile = File(cacheDir, fileName)

            FileWriter(csvFile).use { writer ->
                writer.write(csvContent)
            }

            val authority = "${context.packageName}.fileprovider"
            val fileUri: Uri = FileProvider.getUriForFile(context, authority, csvFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "text/csv"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_SUBJECT, "MeetIQ Productivity & Meeting Analytics Export")
                putExtra(
                    Intent.EXTRA_TEXT,
                    "Attached is the MeetIQ Meeting Analytics & Productivity dataset (CSV) for external analysis in Google Sheets, Excel, or BI tools."
                )
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Export Analytics CSV to")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)

            Toast.makeText(context, "Analytics CSV prepared successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(context, "Failed to export CSV: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
        }
    }

    private fun csvRow(vararg values: Any?): String {
        return values.joinToString(",") { escapeCsv(it?.toString() ?: "") }
    }

    private fun escapeCsv(value: String): String {
        val containsSpecialChars = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r")
        return if (containsSpecialChars) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else {
            value
        }
    }
}
