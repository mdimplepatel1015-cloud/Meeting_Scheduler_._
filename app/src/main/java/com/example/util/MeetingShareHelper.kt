package com.example.util

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.model.ActionItem
import com.example.data.model.Decision
import com.example.data.model.Meeting
import com.example.data.model.MeetingSummary
import com.example.data.model.Participant
import java.io.File
import java.io.FileOutputStream

object MeetingShareHelper {

    fun formatMeetingSummaryText(
        meeting: Meeting,
        summary: MeetingSummary?,
        actionItems: List<ActionItem>,
        decisions: List<Decision>,
        participants: List<Participant>
    ): String {
        return buildString {
            appendLine("📋 MEETIQ EXECUTIVE BRIEFING")
            appendLine("=========================================")
            appendLine("Meeting: ${meeting.title}")
            appendLine("Date & Time: ${meeting.date} at ${meeting.startTime} (${meeting.durationMinutes} mins)")
            appendLine("Location: ${meeting.location}")
            if (meeting.meetingLink.isNotBlank()) {
                appendLine("Link: ${meeting.meetingLink}")
            }
            if (meeting.tags.isNotEmpty()) {
                appendLine("Tags: [${meeting.tags.joinToString("], [")}]")
            }
            appendLine("=========================================")
            appendLine()

            if (summary != null) {
                appendLine("🎯 PRIMARY OBJECTIVE")
                appendLine(summary.objective.ifBlank { "Executive strategic alignment and operational delivery." })
                appendLine()

                appendLine("📝 KEY DISCUSSION POINTS")
                appendLine(summary.keyDiscussionPoints)
                appendLine()

                if (summary.decisionsSummary.isNotBlank()) {
                    appendLine("⚖️ SUMMARY OF DECISIONS")
                    appendLine(summary.decisionsSummary)
                    appendLine()
                }

                if (summary.actionItemsSummary.isNotBlank()) {
                    appendLine("🚀 SUMMARY OF ACTION ITEMS")
                    appendLine(summary.actionItemsSummary)
                    appendLine()
                }

                if (summary.riskFactors.isNotBlank()) {
                    appendLine("⚠️ RISKS & ROADBLOCKS IDENTIFIED")
                    appendLine(summary.riskFactors)
                    appendLine()
                }
            }

            if (decisions.isNotEmpty()) {
                appendLine("⚖️ KEY DECISIONS (${decisions.size})")
                decisions.forEachIndexed { index, decision ->
                    appendLine(" ${index + 1}. ${decision.title}")
                    if (decision.contextNotes.isNotBlank()) {
                        appendLine("    Context: ${decision.contextNotes}")
                    }
                    appendLine("    Owner: ${decision.ownerName} | Impact: ${decision.impactLevel}")
                }
                appendLine()
            }

            if (actionItems.isNotEmpty()) {
                appendLine("🚀 ACTION ITEMS & DELIVERABLES (${actionItems.size})")
                actionItems.forEachIndexed { index, item ->
                    val statusMarker = if (item.status.name == "COMPLETED") "[x]" else "[ ]"
                    appendLine(" $statusMarker ${index + 1}. ${item.title}")
                    appendLine("    Assignee: ${item.assigneeName} | Due: ${item.dueDate} | Priority: ${item.priority}")
                }
                appendLine()
            }

            if (participants.isNotEmpty()) {
                appendLine("👥 ATTENDEES")
                appendLine(participants.joinToString(", ") { "${it.name} (${it.role})" })
                appendLine()
            }

            appendLine("=========================================")
            appendLine("Generated seamlessly with MeetIQ AI Platform")
        }
    }

    fun shareAsFormattedText(
        context: Context,
        meeting: Meeting,
        summary: MeetingSummary?,
        actionItems: List<ActionItem>,
        decisions: List<Decision>,
        participants: List<Participant>
    ) {
        val textContent = formatMeetingSummaryText(meeting, summary, actionItems, decisions, participants)
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_SUBJECT, "MeetIQ Summary: ${meeting.title}")
            putExtra(Intent.EXTRA_TEXT, textContent)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "Share Meeting Summary via")
        shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(shareIntent)
    }

    fun shareAsPdfDocument(
        context: Context,
        meeting: Meeting,
        summary: MeetingSummary?,
        actionItems: List<ActionItem>,
        decisions: List<Decision>,
        participants: List<Participant>
    ) {
        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // Standard A4 (595x842 pt)
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas

            val titlePaint = Paint().apply {
                color = Color.rgb(30, 27, 75) // Dark Indigo
                textSize = 18f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val subtitlePaint = Paint().apply {
                color = Color.rgb(79, 70, 229) // Indigo Accent
                textSize = 11f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val headerPaint = Paint().apply {
                color = Color.rgb(15, 23, 42) // Slate 900
                textSize = 13f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                isAntiAlias = true
            }

            val bodyPaint = Paint().apply {
                color = Color.rgb(51, 65, 85) // Slate 700
                textSize = 10f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val metaPaint = Paint().apply {
                color = Color.rgb(100, 116, 139) // Slate 500
                textSize = 9.5f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
                isAntiAlias = true
            }

            val badgePaint = Paint().apply {
                color = Color.rgb(241, 245, 249)
                style = Paint.Style.FILL
            }

            val linePaint = Paint().apply {
                color = Color.rgb(226, 232, 240)
                strokeWidth = 1f
            }

            var currentY = 40f
            val margin = 36f
            val contentWidth = 595f - (margin * 2)

            // Header Banner
            canvas.drawText("MeetIQ Meeting Briefing & Action Report", margin, currentY, subtitlePaint)
            currentY += 24f

            // Meeting Title
            val titleLines = wrapText(meeting.title, titlePaint, contentWidth)
            titleLines.forEach { line ->
                canvas.drawText(line, margin, currentY, titlePaint)
                currentY += 22f
            }

            currentY += 4f
            // Metadata Line
            val metaInfo = "Date: ${meeting.date} | Time: ${meeting.startTime} (${meeting.durationMinutes}m) | Tags: ${meeting.tags.joinToString(", ")}"
            canvas.drawText(metaInfo, margin, currentY, metaPaint)
            currentY += 14f

            // Divider
            canvas.drawLine(margin, currentY, 595f - margin, currentY, linePaint)
            currentY += 20f

            // Executive Objective & Summary
            if (summary != null) {
                canvas.drawText("EXECUTIVE OBJECTIVE", margin, currentY, headerPaint)
                currentY += 14f
                val objLines = wrapText(summary.objective.ifBlank { "Strategic alignment and execution." }, bodyPaint, contentWidth)
                objLines.forEach { line ->
                    canvas.drawText(line, margin, currentY, bodyPaint)
                    currentY += 13f
                }
                currentY += 10f

                canvas.drawText("EXECUTIVE SUMMARY", margin, currentY, headerPaint)
                currentY += 14f
                val sumLines = wrapText(summary.keyDiscussionPoints, bodyPaint, contentWidth)
                sumLines.take(6).forEach { line ->
                    canvas.drawText(line, margin, currentY, bodyPaint)
                    currentY += 13f
                }
                currentY += 12f
            }

            // Key Decisions
            if (decisions.isNotEmpty() && currentY < 650f) {
                canvas.drawText("KEY DECISIONS (${decisions.size})", margin, currentY, headerPaint)
                currentY += 14f
                decisions.take(3).forEachIndexed { idx, dec ->
                    val decText = "${idx + 1}. ${dec.title} — (Owner: ${dec.ownerName}, Impact: ${dec.impactLevel})"
                    val wrapped = wrapText(decText, bodyPaint, contentWidth)
                    wrapped.forEach { line ->
                        canvas.drawText(line, margin + 4, currentY, bodyPaint)
                        currentY += 12f
                    }
                }
                currentY += 10f
            }

            // Action Items
            if (actionItems.isNotEmpty() && currentY < 750f) {
                canvas.drawText("ACTION ITEMS & DELIVERABLES (${actionItems.size})", margin, currentY, headerPaint)
                currentY += 14f
                actionItems.take(5).forEachIndexed { idx, act ->
                    val marker = if (act.status.name == "COMPLETED") "[✓]" else "[ ]"
                    val actText = "$marker ${act.title} (Assignee: ${act.assigneeName}, Due: ${act.dueDate})"
                    val wrapped = wrapText(actText, bodyPaint, contentWidth)
                    wrapped.forEach { line ->
                        canvas.drawText(line, margin + 4, currentY, bodyPaint)
                        currentY += 12f
                    }
                }
                currentY += 14f
            }

            // Footer
            canvas.drawLine(margin, 800f, 595f - margin, 800f, linePaint)
            canvas.drawText("Generated by MeetIQ AI Executive Assistant", margin, 816f, metaPaint)

            pdfDocument.finishPage(page)

            // Write PDF to cache directory
            val cacheDir = context.cacheDir
            val fileName = "meetiq_${meeting.id.replace("meet_", "")}_summary.pdf"
            val pdfFile = File(cacheDir, fileName)
            FileOutputStream(pdfFile).use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()

            // Share PDF with FileProvider URI
            val authority = "${context.packageName}.fileprovider"
            val contentUri: Uri = FileProvider.getUriForFile(context, authority, pdfFile)

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, contentUri)
                putExtra(Intent.EXTRA_SUBJECT, "MeetIQ PDF Briefing: ${meeting.title}")
                putExtra(Intent.EXTRA_TEXT, "Attached is the official meeting briefing and action items for ${meeting.title}.")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Meeting PDF Briefing")
            chooser.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(context, "Error generating PDF: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
            // Fallback to text share
            shareAsFormattedText(context, meeting, summary, actionItems, decisions, participants)
        }
    }

    private fun wrapText(text: String, paint: Paint, maxWidth: Float): List<String> {
        val words = text.split(" ")
        val lines = mutableListOf<String>()
        var currentLine = StringBuilder()

        for (word in words) {
            val prospectiveLine = if (currentLine.isEmpty()) word else "$currentLine $word"
            if (paint.measureText(prospectiveLine) <= maxWidth) {
                currentLine.append(if (currentLine.isEmpty()) word else " $word")
            } else {
                if (currentLine.isNotEmpty()) {
                    lines.add(currentLine.toString())
                }
                currentLine = StringBuilder(word)
            }
        }
        if (currentLine.isNotEmpty()) {
            lines.add(currentLine.toString())
        }
        return lines
    }
}
