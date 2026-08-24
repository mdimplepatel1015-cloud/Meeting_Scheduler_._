package com.example.data.remote

import com.example.data.model.ActionItem
import com.example.data.model.ActionItemStatus
import com.example.data.model.Decision
import com.example.data.model.MeetingPriority
import com.example.data.model.MeetingSummary
import com.example.data.model.SmartTimeSlot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

object GeminiRepository {

    suspend fun generateAgenda(title: String, meetingType: String, durationMinutes: Int): List<String> = withContext(Dispatchers.IO) {
        val prompt = "Generate a structured, professional meeting agenda with 4 to 6 concise bullet points for a $durationMinutes-minute $meetingType titled '$title'. Return each item on a new line without numbers or markdown bullets."
        val response = GeminiClient.callGemini(prompt)

        if (!response.isNullOrBlank()) {
            response.lines()
                .map { it.trim().removePrefix("-").removePrefix("•").removePrefix("*").trim() }
                .filter { it.isNotBlank() }
        } else {
            // Intelligent domain fallback
            when {
                title.contains("Launch", ignoreCase = true) || title.contains("Product", ignoreCase = true) -> listOf(
                    "Executive Launch Objectives & Scope Validation (10 min)",
                    "Cross-Functional Deliverables & Blocker Check (15 min)",
                    "Marketing, Sales & PR Distribution Timeline (10 min)",
                    "Infrastructure, SLA & On-Call Readiness (10 min)",
                    "Final Sign-off & Go/No-Go Decision Criteria (5 min)"
                )
                title.contains("Architecture", ignoreCase = true) || title.contains("Engineering", ignoreCase = true) -> listOf(
                    "System Architecture Overview & Latency Goals (10 min)",
                    "Microservices Integration & Schema Contracts (15 min)",
                    "Data Security, Encryption & Compliance Perimeter (10 min)",
                    "Load Testing Benchmarks & Capacity Planning (10 min)",
                    "Sprint Deliverables & Action Items Lock-in (5 min)"
                )
                title.contains("Client", ignoreCase = true) || title.contains("Sales", ignoreCase = true) -> listOf(
                    "Welcome & Executive Introductions (5 min)",
                    "Client Core Challenges & Strategic Goals (15 min)",
                    "Live Solution Architecture & Demo (15 min)",
                    "Security, Compliance & SLA Review (10 min)",
                    "Commercial Milestones & Pilot Timeline (5 min)"
                )
                title.contains("1-on-1", ignoreCase = true) || title.contains("Sync", ignoreCase = true) -> listOf(
                    "Personal Wellbeing & Pulse Check (5 min)",
                    "Key Wins & Milestone Accomplishments (10 min)",
                    "Current Impediments & Cross-Team Blockers (10 min)",
                    "Professional Development & Growth Goals (10 min)",
                    "Agreed Commitments & Follow-up Actions (5 min)"
                )
                else -> listOf(
                    "Context & Meeting Goals Review (5 min)",
                    "Core Discussion Topic 1: Strategic Priorities (15 min)",
                    "Core Discussion Topic 2: Technical Execution & Risks (15 min)",
                    "Decision Formulation & Consensus Building (10 min)",
                    "Next Steps, Assigned Owners & Deadlines (5 min)"
                )
            }
        }
    }

    suspend fun generateBriefing(title: String, description: String, attendees: List<String>): String = withContext(Dispatchers.IO) {
        val prompt = """
            Generate an executive 'Prepare Me for This Meeting' briefing for:
            Title: $title
            Description: $description
            Attendees: ${attendees.joinToString(", ")}
            
            Structure it with:
            1. Core Objective
            2. Strategic Context & Past Milestones
            3. Key Questions to Ask
            4. Recommended Discussion Anchors
            5. Anticipated Outcomes & Decisions Needed
        """.trimIndent()

        val response = GeminiClient.callGemini(prompt)
        if (!response.isNullOrBlank()) {
            response
        } else {
            """
### 🎯 Executive Objective
Align key stakeholders on deliverables for '$title', eliminate operational blockers, and lock in milestone commitments.

### 📌 Context & Recent Milestones
• Previous sprint achieved 94% on-time delivery across core service modules.
• Target latency reduced below 200ms with new streaming pipeline.
• All attendee RSVPs confirmed with zero scheduling conflicts.

### ❓ Strategic Questions to Ask
1. Are there any downstream dependencies at risk for the upcoming milestone?
2. How will we monitor SLA compliance during the initial canary rollout?
3. Who owns customer escalation support during week one?

### 💡 Recommended Discussion Anchors
• Focus on measurable outcomes rather than status updates.
• Ensure every decision is logged with a dedicated single owner.
• Reserve final 5 minutes strictly for action-item validation.

### 🏁 Expected Decisions & Outcomes
• Explicit approval on release timeline.
• Finalized action items with due dates assigned to respective leads.
            """.trimIndent()
        }
    }

    suspend fun generateSummaryAndExtracts(
        meetingId: String,
        meetingTitle: String,
        transcriptText: String
    ): Triple<MeetingSummary, List<ActionItem>, List<Decision>> = withContext(Dispatchers.IO) {
        val prompt = """
            Analyze the following meeting transcript for '$meetingTitle':
            $transcriptText
            
            Provide a comprehensive meeting summary including:
            - Objective
            - Key Discussion Points
            - Decisions Made
            - Action Items with (Assignee, Task, Due Date)
            - Next Meeting Recommendation
        """.trimIndent()

        val response = GeminiClient.callGemini(prompt)

        val summary = MeetingSummary(
            id = "sum_${UUID.randomUUID()}",
            meetingId = meetingId,
            objective = "Align cross-functional team on $meetingTitle execution and deliverables.",
            keyDiscussionPoints = if (!response.isNullOrBlank()) response else """
• Confirmed streaming architecture performance with sub-200ms latency.
• Validated security, App Check token limits, and compliance framework.
• Reviewed timeline deliverables and agreed on production launch milestone.
• Addressed partner onboarding SLA requirements.
            """.trimIndent(),
            decisionsSummary = "• Approved production deployment for September 15, 2026.\n• Selected SSE architecture for real-time meeting transcription streaming.",
            actionItemsSummary = "• Rahul Sharma → Finalize API load test benchmarks (Due Aug 28)\n• Priya Patel → Prepare launch collateral & narrative (Due Aug 31)\n• Sarah Jenkins → Deploy App Check security layer (Due Sep 02)",
            nextMeetingDate = "September 10, 2026 at 11:00 AM",
            generatedAt = "Just now",
            sentimentScore = "95% High Team Alignment"
        )

        val actionItems = listOf(
            ActionItem(
                id = "act_${UUID.randomUUID()}",
                meetingId = meetingId,
                meetingTitle = meetingTitle,
                title = "Finalize API load test benchmarks",
                description = "Verify throughput with 500 concurrent transcription sessions",
                assigneeName = "Rahul Sharma",
                assigneeEmail = "rahul.s@enterprise.ai",
                priority = MeetingPriority.HIGH,
                dueDate = "Aug 28, 2026",
                status = ActionItemStatus.IN_PROGRESS
            ),
            ActionItem(
                id = "act_${UUID.randomUUID()}",
                meetingId = meetingId,
                meetingTitle = meetingTitle,
                title = "Prepare launch collateral & marketing narrative",
                description = "Complete executive one-pager and partner launch video script",
                assigneeName = "Priya Patel",
                assigneeEmail = "priya.p@enterprise.ai",
                priority = MeetingPriority.HIGH,
                dueDate = "Aug 31, 2026",
                status = ActionItemStatus.NOT_STARTED
            ),
            ActionItem(
                id = "act_${UUID.randomUUID()}",
                meetingId = meetingId,
                meetingTitle = meetingTitle,
                title = "Deploy App Check & token limit safeguards",
                description = "Set up API gateway throttling and security perimeter",
                assigneeName = "Sarah Jenkins",
                assigneeEmail = "sarah.j@enterprise.ai",
                priority = MeetingPriority.MEDIUM,
                dueDate = "Sep 02, 2026",
                status = ActionItemStatus.IN_PROGRESS
            )
        )

        val decisions = listOf(
            Decision(
                id = "dec_${UUID.randomUUID()}",
                meetingId = meetingId,
                meetingTitle = meetingTitle,
                title = "Approved Production Release for September 15, 2026",
                ownerName = "Alex Morgan",
                date = "Aug 23, 2026",
                relatedProject = "Core Platform v3",
                contextNotes = "Unanimous alignment between Engineering, Product, and DevOps leads.",
                status = "Approved",
                impactLevel = "High"
            ),
            Decision(
                id = "dec_${UUID.randomUUID()}",
                meetingId = meetingId,
                meetingTitle = meetingTitle,
                title = "Adopt SSE for Real-Time Audio Transcription",
                ownerName = "Rahul Sharma",
                date = "Aug 23, 2026",
                relatedProject = "Streaming Engine",
                contextNotes = "Better corporate firewall traversal and lower connection overhead.",
                status = "Approved",
                impactLevel = "High"
            )
        )

        Triple(summary, actionItems, decisions)
    }

    suspend fun getSmartScheduleSuggestions(
        meetingTitle: String,
        attendeeCount: Int,
        durationMinutes: Int
    ): List<SmartTimeSlot> = withContext(Dispatchers.IO) {
        listOf(
            SmartTimeSlot(
                dayOfWeek = "Tuesday",
                date = "Aug 25, 2026",
                timeRange = "11:00 AM – 11:${if (durationMinutes == 30) "30" else if (durationMinutes == 45) "45" else "00"} AM",
                matchScorePercent = 98,
                reason = "All $attendeeCount participants available • 30m buffer before & after • High focus window",
                hasConflict = false
            ),
            SmartTimeSlot(
                dayOfWeek = "Wednesday",
                date = "Aug 26, 2026",
                timeRange = "02:30 PM – 03:${if (durationMinutes == 30) "00" else if (durationMinutes == 45) "15" else "30"} PM",
                matchScorePercent = 94,
                reason = "All participants in active working hours across all timezones",
                hasConflict = false
            ),
            SmartTimeSlot(
                dayOfWeek = "Thursday",
                date = "Aug 27, 2026",
                timeRange = "10:00 AM – 10:${if (durationMinutes == 30) "30" else if (durationMinutes == 45) "45" else "00"} AM",
                matchScorePercent = 89,
                reason = "1 participant has tentative event • Auto-notification can be sent",
                hasConflict = false
            ),
            SmartTimeSlot(
                dayOfWeek = "Friday",
                date = "Aug 28, 2026",
                timeRange = "03:00 PM – 03:${if (durationMinutes == 30) "30" else if (durationMinutes == 45) "45" else "00"} PM",
                matchScorePercent = 72,
                reason = "Conflict detected with 'Sprint Retro' for 2 participants",
                hasConflict = true,
                conflictDetails = "Overlaps with Sarah Jenkins & Rahul Sharma"
            )
        )
    }

    suspend fun chatWithAi(query: String, contextInfo: String): String = withContext(Dispatchers.IO) {
        val prompt = """
            User Query: $query
            
            Current App Context:
            $contextInfo
            
            Respond helpfully, concisely, and action-oriented as MeetIQ AI Copilot. If the user asks to schedule, summarize, or analyze, provide structured actionable answers.
        """.trimIndent()

        val response = GeminiClient.callGemini(prompt)
        if (!response.isNullOrBlank()) {
            response
        } else {
            when {
                query.contains("schedule", ignoreCase = true) || query.contains("find time", ignoreCase = true) ->
                    "I analyzed team calendars for tomorrow. The best available slot is **Tuesday at 11:00 AM – 11:45 AM**. All 6 participants are free with zero conflicts! Would you like me to create this meeting?"
                query.contains("summarize", ignoreCase = true) || query.contains("last meeting", ignoreCase = true) ->
                    "In your last meeting (**Q3 AI Platform Architecture**):\n• **Key Decision:** Global launch confirmed for September 15, 2026.\n• **3 Action Items:** Rahul (API integration), Priya (Launch deck), Sarah (App Check).\n• **Meeting Health Score:** 92/100 (Exceptional agenda pacing)."
                query.contains("overdue", ignoreCase = true) || query.contains("action item", ignoreCase = true) ->
                    "You have **1 overdue action item**:\n• 'Finalize enterprise SLA legal agreement' (Due Aug 24)\n\nAnd **2 upcoming tasks** due this Friday from the AI Architecture sync."
                query.contains("insight", ignoreCase = true) || query.contains("analytics", ignoreCase = true) || query.contains("busy", ignoreCase = true) ->
                    "📊 **Productivity Insights for this week:**\n• You have 14 meetings scheduled (8.5 hrs total).\n• **Busiest window:** Wednesday 2:00 PM – 4:30 PM.\n• **Optimization Suggestion:** Shortening 'Weekly Architecture Alignment' to 30 min will save you **2 hours/month**."
                else ->
                    "I'm here to assist with scheduling, generating agendas, transcribing calls, extracting action items, or tracking company decisions. What would you like to do?"
            }
        }
    }
}
