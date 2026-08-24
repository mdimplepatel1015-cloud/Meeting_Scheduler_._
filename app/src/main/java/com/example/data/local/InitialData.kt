package com.example.data.local

import com.example.data.model.ActionItem
import com.example.data.model.ActionItemStatus
import com.example.data.model.AgendaItem
import com.example.data.model.AuditLog
import com.example.data.model.ChatMessage
import com.example.data.model.Decision
import com.example.data.model.Meeting
import com.example.data.model.MeetingNote
import com.example.data.model.MeetingPriority
import com.example.data.model.MeetingRecurrence
import com.example.data.model.MeetingStatus
import com.example.data.model.MeetingSummary
import com.example.data.model.MeetingTemplate
import com.example.data.model.MeetingType
import com.example.data.model.MeetingVisibility
import com.example.data.model.NotificationItem
import com.example.data.model.NotificationType
import com.example.data.model.Participant
import com.example.data.model.RsvpStatus
import com.example.data.model.TranscriptLine
import com.example.data.model.User
import com.example.data.model.UserRole

object InitialData {
    val defaultUser = User(
        id = "usr_current",
        name = "Alex Morgan",
        email = "alex.morgan@enterprise.ai",
        role = UserRole.MANAGER,
        avatarInitials = "AM",
        avatarColorHex = "#4F46E5",
        timezone = "America/Los_Angeles (PST)",
        workingHoursStart = "09:00",
        workingHoursEnd = "17:30",
        preferredDurationMinutes = 30,
        preferredDays = "Mon, Tue, Wed, Thu, Fri",
        emailNotifications = true,
        pushNotifications = true,
        aiMeetingAnalysis = true,
        autoSummarize = true,
        language = "English (US)"
    )

    val sampleMeetings = listOf(
        Meeting(
            id = "meet_1",
            title = "Q3 AI Platform Architecture & Roadmap",
            description = "Quarterly technical alignment on scalable microservices, Gemini API streaming integrations, and data residency governance.",
            type = MeetingType.PROJECT_MEETING,
            category = "Engineering",
            date = "2026-08-23",
            startTime = "10:00 AM",
            endTime = "10:45 AM",
            startTimestamp = System.currentTimeMillis() - 3600000,
            durationMinutes = 45,
            timezone = "PST (UTC-7)",
            location = "Google Meet (Room 4A)",
            meetingLink = "https://meet.google.com/q3-arch-meetiq",
            priority = MeetingPriority.HIGH,
            reminderMinutesBefore = 15,
            recurrence = MeetingRecurrence.NONE,
            visibility = MeetingVisibility.PRIVATE,
            status = MeetingStatus.COMPLETED,
            healthScore = 92,
            healthScoreExplanation = "Exemplary agenda pacing, clear owner assignment for all 3 tasks, and high decision clarity on streaming pipeline.",
            isRecurring = false,
            preparationBriefing = "Focus on backend latency targets (<200ms) and cost breakdown for enterprise inference.",
            recordingActive = false,
            hasTranscript = true,
            hasSummary = true,
            tags = listOf("Work", "Architecture", "Engineering")
        ),
        Meeting(
            id = "meet_2",
            title = "Executive Product Launch Alignment",
            description = "Final go-to-market readiness check, marketing collateral approval, and customer onboarding SLA review.",
            type = MeetingType.TEAM_MEETING,
            category = "Product",
            date = "2026-08-23",
            startTime = "02:00 PM",
            endTime = "02:45 PM",
            startTimestamp = System.currentTimeMillis() + 7200000,
            durationMinutes = 45,
            timezone = "PST (UTC-7)",
            location = "Zoom Executive Suite",
            meetingLink = "https://zoom.us/j/987123456",
            priority = MeetingPriority.HIGH,
            reminderMinutesBefore = 30,
            recurrence = MeetingRecurrence.NONE,
            visibility = MeetingVisibility.CONFIDENTIAL,
            status = MeetingStatus.UPCOMING,
            healthScore = 88,
            healthScoreExplanation = "All 6 required stakeholders have confirmed RSVP. Agenda items pre-allocated.",
            isRecurring = false,
            preparationBriefing = "Key questions for Priya: Confirm press release distribution timeline. Review Rahul's API load test numbers.",
            tags = listOf("Work", "Product", "Executive")
        ),
        Meeting(
            id = "meet_3",
            title = "1-on-1: Career Growth & Sprint Retrospective",
            description = "Bi-weekly personal sync with Sarah Jenkins regarding leadership path, mentorship goals, and current blockers.",
            type = MeetingType.ONE_ON_ONE,
            category = "People",
            date = "2026-08-23",
            startTime = "04:30 PM",
            endTime = "05:00 PM",
            startTimestamp = System.currentTimeMillis() + 16200000,
            durationMinutes = 30,
            timezone = "PST (UTC-7)",
            location = "Slack Huddle",
            meetingLink = "https://enterprise.slack.com/huddle/sarah-alex",
            priority = MeetingPriority.MEDIUM,
            reminderMinutesBefore = 15,
            recurrence = MeetingRecurrence.BIWEEKLY,
            visibility = MeetingVisibility.PRIVATE,
            status = MeetingStatus.UPCOMING,
            healthScore = 95,
            healthScoreExplanation = "Balanced conversational space, high trust historical score.",
            isRecurring = true,
            recurringInsight = "Consistently high engagement and actionable feedback loops.",
            tags = listOf("Personal", "1-on-1", "People")
        ),
        Meeting(
            id = "meet_4",
            title = "Weekly Architecture & Infra Alignment",
            description = "Review recurring cloud infrastructure spend, Kubernetes cluster scaling, and CI/CD throughput.",
            type = MeetingType.REVIEW_MEETING,
            category = "DevOps",
            date = "2026-08-24",
            startTime = "11:00 AM",
            endTime = "12:00 PM",
            startTimestamp = System.currentTimeMillis() + 86400000,
            durationMinutes = 60,
            timezone = "PST (UTC-7)",
            location = "Google Meet",
            meetingLink = "https://meet.google.com/infra-weekly-ops",
            priority = MeetingPriority.MEDIUM,
            reminderMinutesBefore = 15,
            recurrence = MeetingRecurrence.WEEKLY,
            visibility = MeetingVisibility.PUBLIC,
            status = MeetingStatus.UPCOMING,
            healthScore = 74,
            healthScoreExplanation = "AI Recommendation: Regularly finishes in 35m. Suggest shortening from 60m to 30m to recover 2.0 hrs/month.",
            isRecurring = true,
            recurringInsight = "Meeting frequently finishes 25 min early with high agenda completion.",
            tags = listOf("Work", "DevOps", "Architecture")
        ),
        Meeting(
            id = "meet_5",
            title = "Enterprise Client Discovery: Meridian Health",
            description = "Initial technical requirement gathering with Meridian Health CTO and data compliance officers.",
            type = MeetingType.CLIENT_MEETING,
            category = "Sales",
            date = "2026-08-25",
            startTime = "01:00 PM",
            endTime = "01:45 PM",
            startTimestamp = System.currentTimeMillis() + 172800000,
            durationMinutes = 45,
            timezone = "PST (UTC-7)",
            location = "Microsoft Teams",
            meetingLink = "https://teams.microsoft.com/l/meetup-join/meridian",
            priority = MeetingPriority.HIGH,
            reminderMinutesBefore = 60,
            recurrence = MeetingRecurrence.NONE,
            visibility = MeetingVisibility.CONFIDENTIAL,
            status = MeetingStatus.UPCOMING,
            healthScore = 89,
            healthScoreExplanation = "Preparation dossier generated. HIPAA compliance and SOC2 collateral attached.",
            isRecurring = false,
            tags = listOf("Client", "Sales", "Work")
        )
    )

    val sampleParticipants = listOf(
        // For meet_1
        Participant("p1_1", "meet_1", "Alex Morgan", "alex.morgan@enterprise.ai", "Organizer", RsvpStatus.ACCEPTED, isOrganizer = true, avatarColor = "#4F46E5", speakingPercentage = 38, speakingTimeSeconds = 680),
        Participant("p1_2", "meet_1", "Rahul Sharma", "rahul.s@enterprise.ai", "Tech Lead", RsvpStatus.ACCEPTED, isOrganizer = false, avatarColor = "#06B6D4", speakingPercentage = 34, speakingTimeSeconds = 610),
        Participant("p1_3", "meet_1", "Priya Patel", "priya.p@enterprise.ai", "Product Director", RsvpStatus.ACCEPTED, isOrganizer = false, avatarColor = "#EC4899", speakingPercentage = 18, speakingTimeSeconds = 320),
        Participant("p1_4", "meet_1", "Sarah Jenkins", "sarah.j@enterprise.ai", "Senior Dev", RsvpStatus.ACCEPTED, isOrganizer = false, avatarColor = "#10B981", speakingPercentage = 10, speakingTimeSeconds = 180),

        // For meet_2
        Participant("p2_1", "meet_2", "Alex Morgan", "alex.morgan@enterprise.ai", "Host", RsvpStatus.ACCEPTED, isOrganizer = true, avatarColor = "#4F46E5"),
        Participant("p2_2", "meet_2", "Priya Patel", "priya.p@enterprise.ai", "Presenter", RsvpStatus.ACCEPTED, isOrganizer = false, avatarColor = "#EC4899"),
        Participant("p2_3", "meet_2", "Rahul Sharma", "rahul.s@enterprise.ai", "Attendee", RsvpStatus.ACCEPTED, isOrganizer = false, avatarColor = "#06B6D4"),
        Participant("p2_4", "meet_2", "Michael Chen", "michael.c@enterprise.ai", "Marketing VP", RsvpStatus.ACCEPTED, isOrganizer = false, avatarColor = "#F59E0B"),
        Participant("p2_5", "meet_2", "David Ross", "david.r@enterprise.ai", "Sales Lead", RsvpStatus.MAYBE, isOrganizer = false, avatarColor = "#8B5CF6", isOptional = true),

        // For meet_3
        Participant("p3_1", "meet_3", "Alex Morgan", "alex.morgan@enterprise.ai", "Manager", RsvpStatus.ACCEPTED, isOrganizer = true, avatarColor = "#4F46E5"),
        Participant("p3_2", "meet_3", "Sarah Jenkins", "sarah.j@enterprise.ai", "Direct Report", RsvpStatus.ACCEPTED, isOrganizer = false, avatarColor = "#10B981")
    )

    val sampleAgendaItems = listOf(
        AgendaItem("ag_1_1", "meet_1", 1, "Review Current API Latency Benchmarks", 10, "Rahul Sharma", "Examine P95 & P99 response times", true),
        AgendaItem("ag_1_2", "meet_1", 2, "Gemini 3.5 Flash Streaming Pipeline Architecture", 15, "Alex Morgan", "Discuss SSE protocol and caching layer", true),
        AgendaItem("ag_1_3", "meet_1", 3, "Security, Token Limits & App Check Integration", 10, "Sarah Jenkins", "Review enterprise security perimeter", true),
        AgendaItem("ag_1_4", "meet_1", 4, "Action Items Assignment & Sprint Commitments", 10, "Priya Patel", "Lock in release timeline deliverables", true),

        AgendaItem("ag_2_1", "meet_2", 1, "GTM Launch Readiness Checklist", 15, "Priya Patel", "Review status across all functional departments", false),
        AgendaItem("ag_2_2", "meet_2", 2, "Customer Support Runbook & SLA Validation", 15, "Michael Chen", "Ensure tier-1 escalations are operational", false),
        AgendaItem("ag_2_3", "meet_2", 3, "Rollout Staging & Regional Phasing Plan", 15, "Alex Morgan", "Approve 10% canary to 100% schedule", false)
    )

    val sampleNotes = listOf(
        MeetingNote("n_1", "meet_1", "Alex Morgan", "Stream payload compression reduced network serialization overhead by 42%.", false),
        MeetingNote("n_2", "meet_1", "Priya Patel", "Marketing team needs API stability confirmation by Thursday for partner demo.", false),
        MeetingNote("n_3", "meet_1", "Alex Morgan", "Private Note: Ensure Rahul has backup engineer for Friday deployment window.", true)
    )

    val sampleTranscript = listOf(
        TranscriptLine("tr_1", "meet_1", "Alex Morgan", "00:45", "Welcome everyone. Today our primary goal is finalizing the Q3 AI streaming pipeline architecture and locking down dates."),
        TranscriptLine("tr_2", "meet_1", "Rahul Sharma", "03:12", "Thanks Alex. I tested the Gemini 3.5 Flash endpoint with OkHttp 60-second timeouts. Latency dropped to 180ms on regional servers."),
        TranscriptLine("tr_3", "meet_1", "Priya Patel", "08:30", "That speed will significantly improve our real-time meeting transcription experience. Can we commit to September 15 for launch?"),
        TranscriptLine("tr_4", "meet_1", "Alex Morgan", "12:15", "Yes, let's officially lock September 15 as the global release milestone.", isDecision = true),
        TranscriptLine("tr_5", "meet_1", "Rahul Sharma", "15:40", "I will complete the API integration and load test verification by this Friday.", isActionItem = true),
        TranscriptLine("tr_6", "meet_1", "Priya Patel", "22:10", "I'll prepare the marketing launch narrative and partner collateral by Monday.", isActionItem = true),
        TranscriptLine("tr_7", "meet_1", "Sarah Jenkins", "29:05", "I'll implement the token limit safeguards and App Check security layer before Wednesday.", isActionItem = true),
        TranscriptLine("tr_8", "meet_1", "Alex Morgan", "41:20", "Fantastic alignment everyone. Meeting adjourned.")
    )

    val sampleSummary = MeetingSummary(
        id = "sum_1",
        meetingId = "meet_1",
        objective = "Finalize Q3 AI Platform architecture, streaming pipeline, and confirm launch milestones.",
        keyDiscussionPoints = "• Evaluated Gemini 3.5 Flash streaming performance with sub-200ms latency.\n• Reviewed OkHttp 60-second timeout configuration for long-running summaries.\n• Addressed Token Limit caching and App Check enterprise security.\n• Confirmed customer onboarding SLA requirements for enterprise partners.",
        decisionsSummary = "• Approved global platform rollout date for September 15, 2026.\n• Selected SSE streaming architecture with Moshi/JSON serialization for real-time transcription.",
        actionItemsSummary = "• Rahul Sharma → Complete API integration & load testing → Due Friday (Aug 28)\n• Priya Patel → Prepare marketing launch collateral → Due Monday (Aug 31)\n• Sarah Jenkins → Implement App Check & token limits → Due Wednesday (Sept 2)",
        nextMeetingDate = "September 10, 2026 at 11:00 AM",
        generatedAt = "Aug 23, 2026 • 10:48 AM",
        sentimentScore = "Highly Collaborative (96% Efficiency)",
        riskFactors = "Third-party vendor API rate limits during peak usage hours."
    )

    val sampleActionItems = listOf(
        ActionItem("act_1", "meet_1", "Q3 AI Platform Architecture", "Complete API integration & load test verification", "Stress test 500 concurrent transcription streams", "Rahul Sharma", "rahul.s@enterprise.ai", MeetingPriority.HIGH, "Aug 28, 2026", ActionItemStatus.IN_PROGRESS),
        ActionItem("act_2", "meet_1", "Q3 AI Platform Architecture", "Prepare marketing launch narrative & partner deck", "Draft executive one-pager and video script", "Priya Patel", "priya.p@enterprise.ai", MeetingPriority.HIGH, "Aug 31, 2026", ActionItemStatus.NOT_STARTED),
        ActionItem("act_3", "meet_1", "Q3 AI Platform Architecture", "Implement token limit safeguards & App Check", "Add rate-limiter and credential check fallback", "Sarah Jenkins", "sarah.j@enterprise.ai", MeetingPriority.MEDIUM, "Sep 02, 2026", ActionItemStatus.IN_PROGRESS),
        ActionItem("act_4", "meet_2", "Executive Product Launch Alignment", "Finalize enterprise SLA legal agreement", "Review clause 4.2 with legal team", "Alex Morgan", "alex.morgan@enterprise.ai", MeetingPriority.HIGH, "Aug 24, 2026", ActionItemStatus.OVERDUE),
        ActionItem("act_5", "meet_3", "1-on-1: Career Growth", "Draft Q4 engineering mentorship proposal", "Outline objectives for junior developer pairing", "Sarah Jenkins", "sarah.j@enterprise.ai", MeetingPriority.LOW, "Sep 05, 2026", ActionItemStatus.COMPLETED)
    )

    val sampleDecisions = listOf(
        Decision("dec_1", "meet_1", "Q3 AI Platform Architecture", "Launch date locked for September 15, 2026", "Alex Morgan", "Aug 23, 2026", "Core Platform v3", "Unanimous alignment from Engineering, Product, and DevOps.", "Approved", "High"),
        Decision("dec_2", "meet_1", "Q3 AI Platform Architecture", "Adopt Server-Sent Events (SSE) for Real-Time Streaming", "Rahul Sharma", "Aug 23, 2026", "Streaming Engine", "Selected over WebSockets due to reduced firewall proxy complications.", "Approved", "High"),
        Decision("dec_3", "meet_4", "Weekly Architecture & Infra Alignment", "Migrate staging clusters to Graviton ARM nodes", "Alex Morgan", "Aug 17, 2026", "Infrastructure", "Reduces monthly cloud compute bill by 28%.", "Approved", "Medium")
    )

    val sampleNotifications = listOf(
        NotificationItem("notif_1", "Meeting Starting in 15 Minutes", "Executive Product Launch Alignment with Priya, Rahul, and Michael.", NotificationType.STARTING_SOON, System.currentTimeMillis() - 600000, "10m ago", false, "meet_2"),
        NotificationItem("notif_2", "AI Summary & Decisions Ready", "Summary generated for Q3 AI Platform Architecture & Roadmap.", NotificationType.SUMMARY_READY, System.currentTimeMillis() - 3600000, "1h ago", false, "meet_1"),
        NotificationItem("notif_3", "New Action Item Assigned", "You were assigned 'Finalize enterprise SLA legal agreement' by Priya Patel.", NotificationType.ACTION_ASSIGNED, System.currentTimeMillis() - 7200000, "2h ago", true, "meet_2"),
        NotificationItem("notif_4", "New Invitation Received", "Sarah Jenkins invited you to 'Design System & M3 Migration Huddle'.", NotificationType.INVITATION, System.currentTimeMillis() - 14400000, "4h ago", true, "meet_5"),
        NotificationItem("notif_5", "Smart AI Optimization Insight", "Weekly Infra Alignment can be reduced from 60m to 30m to save 2 hrs/mo.", NotificationType.AI_INSIGHT, System.currentTimeMillis() - 86400000, "1d ago", true, "meet_4")
    )

    val sampleTemplates = listOf(
        MeetingTemplate("tpl_1", "Daily Standup", "Fast-paced 15-minute sync on yesterday, today, and blockers.", MeetingType.TEAM_MEETING, 15, "Agile", "Yesterday's Accomplishments, Today's Focus, Blockers & Impediments, Urgent Help Needed", MeetingPriority.HIGH, MeetingRecurrence.DAILY, "timer"),
        MeetingTemplate("tpl_2", "Weekly Team Sync", "Comprehensive team alignment, OKR check-in, and sprint priorities.", MeetingType.TEAM_MEETING, 45, "Management", "Wins & Shoutouts, Metrics & OKRs Progress, Project Deep Dives, Team Challenges, Next Week Priorities", MeetingPriority.HIGH, MeetingRecurrence.WEEKLY, "group"),
        MeetingTemplate("tpl_3", "1-on-1 Feedback & Growth", "Structured 30-minute private check-in between manager and report.", MeetingType.ONE_ON_ONE, 30, "People", "Personal Pulse & Wellbeing, Current Priorities & Progress, Roadblocks & Support Needed, Career Growth & Feedback, Action Items", MeetingPriority.MEDIUM, MeetingRecurrence.BIWEEKLY, "person"),
        MeetingTemplate("tpl_4", "Client Discovery & Demo", "Professional presentation, requirements gathering, and solution pitch.", MeetingType.CLIENT_MEETING, 45, "Sales", "Introductions & Executive Overview, Client Pain Points & Objectives, Live Product Demonstration, Technical Architecture & Compliance, Commercial Timeline & Next Steps", MeetingPriority.HIGH, MeetingRecurrence.NONE, "handshake"),
        MeetingTemplate("tpl_5", "Sprint Retrospective", "Reflective session analyzing what went well, what didn't, and action points.", MeetingType.REVIEW_MEETING, 45, "Agile", "Sprint Metrics Review, What Went Great (Celebrate), What Could Be Better, Root Cause Brainstorming, Top 3 Commitments for Next Sprint", MeetingPriority.MEDIUM, MeetingRecurrence.BIWEEKLY, "history"),
        MeetingTemplate("tpl_6", "Executive Architecture Review", "Deep-dive technical assessment for major architectural shifts and security.", MeetingType.PROJECT_MEETING, 60, "Engineering", "System Requirements & Context, Architectural Diagrams & Tradeoffs, Security & Data Residency, Cost & Latency Projections, Decision Sign-off", MeetingPriority.HIGH, MeetingRecurrence.NONE, "memory"),
        MeetingTemplate("tpl_7", "Creative Brainstorming & Ideation", "Collaborative design-thinking workshop for new features and solutions.", MeetingType.BRAINSTORMING, 45, "Innovation", "Problem Framing & Customer Quotes, Silent Divergent Brainstorming (Sticky Notes), Group Clustering & Theme Mapping, Dot Voting & Prioritization, Concept Prototype Owner", MeetingPriority.MEDIUM, MeetingRecurrence.NONE, "psychology")
    )

    val sampleAuditLogs = listOf(
        AuditLog("log_1", "User Role Modified", "Alex Morgan", "Admin", "Today, 09:15 AM", "Granted Manager role to Sarah Jenkins", "Security"),
        AuditLog("log_2", "Calendar Synced", "System", "Service", "Today, 08:30 AM", "Google Calendar bidirectional sync completed (14 events)", "Integration"),
        AuditLog("log_3", "AI Summary Generated", "MeetIQ AI Engine", "AI System", "Today, 10:48 AM", "Generated summary and 3 action items for meet_1", "AI"),
        AuditLog("log_4", "Data Export Initiated", "Alex Morgan", "Admin", "Yesterday, 04:20 PM", "Exported corporate decision log archive (JSON/CSV)", "Security")
    )

    val sampleChatMessages = listOf(
        ChatMessage("cm_1", "meet_1", "Sarah Jenkins", "Dropped the link to the latency dashboard in the notes.", "10:14 AM"),
        ChatMessage("cm_2", "meet_1", "Rahul Sharma", "Great, looking at it now. P99 is under 190ms.", "10:16 AM"),
        ChatMessage("cm_3", "meet_1", "MeetIQ AI", "Action item detected: 'Rahul to complete load testing by Friday'. Added to tracker.", "10:17 AM", isAiAssistant = true),
        ChatMessage("cm_4", "meet_1", "Priya Patel", "Awesome AI capture!", "10:18 AM")
    )
}
