package com.example.data.repository

import com.example.data.local.InitialData
import com.example.data.local.MeetingDao
import com.example.data.local.UserDao
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
import com.example.data.model.SmartTimeSlot
import com.example.data.model.TranscriptLine
import com.example.data.model.User
import com.example.data.remote.GeminiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.withContext
import java.util.UUID

class MeetingRepository(
    private val meetingDao: MeetingDao,
    private val userDao: UserDao
) {
    val allMeetings: Flow<List<Meeting>> = meetingDao.getAllMeetings()
    val allActionItems: Flow<List<ActionItem>> = meetingDao.getAllActionItems()
    val allDecisions: Flow<List<Decision>> = meetingDao.getAllDecisions()
    val allMeetingSummaries: Flow<List<MeetingSummary>> = meetingDao.getAllMeetingSummaries()
    val allNotifications: Flow<List<NotificationItem>> = meetingDao.getAllNotifications()
    val allTemplates: Flow<List<MeetingTemplate>> = meetingDao.getAllTemplates()
    val allAuditLogs: Flow<List<AuditLog>> = meetingDao.getAllAuditLogs()
    val currentUser: Flow<User?> = userDao.getUser()

    fun getMeetingById(id: String): Flow<Meeting?> = meetingDao.getMeetingById(id)
    fun searchMeetings(query: String): Flow<List<Meeting>> = meetingDao.searchMeetingsByTitleOrParticipant(query)
    fun searchMeetingsWithTag(query: String, tag: String): Flow<List<Meeting>> = meetingDao.searchMeetingsByTagAndQuery(query, tag)
    fun getMeetingsByTag(tag: String): Flow<List<Meeting>> = meetingDao.getMeetingsByTag(tag)
    suspend fun getUpcomingMeetingsListSync(): List<Meeting> = meetingDao.getUpcomingMeetingsListSync()
    fun getParticipants(meetingId: String): Flow<List<Participant>> = meetingDao.getParticipantsForMeeting(meetingId)
    fun getAgenda(meetingId: String): Flow<List<AgendaItem>> = meetingDao.getAgendaForMeeting(meetingId)
    fun getNotes(meetingId: String): Flow<List<MeetingNote>> = meetingDao.getNotesForMeeting(meetingId)
    fun getTranscript(meetingId: String): Flow<List<TranscriptLine>> = meetingDao.getTranscriptForMeeting(meetingId)
    fun getSummary(meetingId: String): Flow<MeetingSummary?> = meetingDao.getSummaryForMeeting(meetingId)
    fun getActionItemsForMeeting(meetingId: String): Flow<List<ActionItem>> = meetingDao.getActionItemsForMeeting(meetingId)
    fun getDecisionsForMeeting(meetingId: String): Flow<List<Decision>> = meetingDao.getDecisionsForMeeting(meetingId)
    suspend fun getMeetingByIdSync(id: String): Meeting? = meetingDao.getMeetingByIdSync(id)
    suspend fun getAllMeetingsSync(): List<Meeting> = meetingDao.getAllMeetingsSync()
    suspend fun importCalendarMeetings(meetings: List<Meeting>) = withContext(Dispatchers.IO) {
        meetingDao.insertMeetings(meetings)
    }
    suspend fun addTranscriptLine(line: TranscriptLine) = withContext(Dispatchers.IO) {
        meetingDao.insertTranscriptLine(line)
        val meeting = meetingDao.getMeetingByIdSync(line.meetingId)
        if (meeting != null && !meeting.hasTranscript) {
            meetingDao.updateMeeting(meeting.copy(hasTranscript = true))
        }
    }
    fun getChatMessages(meetingId: String): Flow<List<ChatMessage>> = meetingDao.getChatMessages(meetingId)

    suspend fun seedInitialDataIfNeeded() = withContext(Dispatchers.IO) {
        val existing = userDao.getUserSync()
        if (existing == null) {
            userDao.insertUser(InitialData.defaultUser)
            meetingDao.insertMeetings(InitialData.sampleMeetings)
            meetingDao.insertParticipants(InitialData.sampleParticipants)
            meetingDao.insertAgendaItems(InitialData.sampleAgendaItems)
            InitialData.sampleNotes.forEach { meetingDao.insertNote(it) }
            meetingDao.insertTranscriptLines(InitialData.sampleTranscript)
            meetingDao.insertSummary(InitialData.sampleSummary)
            meetingDao.insertActionItems(InitialData.sampleActionItems)
            meetingDao.insertDecisions(InitialData.sampleDecisions)
            meetingDao.insertNotifications(InitialData.sampleNotifications)
            meetingDao.insertTemplates(InitialData.sampleTemplates)
            meetingDao.insertAuditLogs(InitialData.sampleAuditLogs)
            InitialData.sampleChatMessages.forEach { meetingDao.insertChatMessage(it) }
        }
    }

    suspend fun createMeeting(
        title: String,
        description: String,
        type: MeetingType,
        category: String,
        date: String,
        startTime: String,
        endTime: String,
        durationMinutes: Int,
        timezone: String,
        location: String,
        meetingLink: String,
        priority: MeetingPriority,
        reminderMinutesBefore: Int,
        recurrence: MeetingRecurrence,
        visibility: MeetingVisibility,
        attendeeEmails: List<String>,
        agendaTitles: List<String>,
        tags: List<String> = listOf("Work")
    ): String = withContext(Dispatchers.IO) {
        val meetingId = "meet_${UUID.randomUUID().toString().take(8)}"
        val meeting = Meeting(
            id = meetingId,
            title = title,
            description = description,
            type = type,
            category = category,
            date = date,
            startTime = startTime,
            endTime = endTime,
            startTimestamp = System.currentTimeMillis() + 86400000,
            durationMinutes = durationMinutes,
            timezone = timezone,
            location = location,
            meetingLink = meetingLink.ifBlank { "https://meet.google.com/${UUID.randomUUID().toString().take(6)}" },
            priority = priority,
            reminderMinutesBefore = reminderMinutesBefore,
            recurrence = recurrence,
            visibility = visibility,
            status = MeetingStatus.UPCOMING,
            healthScore = 88,
            healthScoreExplanation = "Standard pre-meeting preparation profile.",
            isRecurring = recurrence != MeetingRecurrence.NONE,
            tags = tags.ifEmpty { listOf("Work") }
        )
        meetingDao.insertMeeting(meeting)

        // Add Host
        val user = userDao.getUserSync() ?: InitialData.defaultUser
        val host = Participant(
            id = "p_${UUID.randomUUID().toString().take(8)}",
            meetingId = meetingId,
            name = user.name,
            email = user.email,
            role = "Host",
            rsvpStatus = RsvpStatus.ACCEPTED,
            isOrganizer = true,
            avatarColor = "#4F46E5"
        )
        meetingDao.insertParticipant(host)

        // Add Attendees
        attendeeEmails.forEach { email ->
            val cleanEmail = email.trim()
            if (cleanEmail.isNotBlank()) {
                val name = cleanEmail.substringBefore("@").replace(".", " ").split(" ")
                    .joinToString(" ") { it.replaceFirstChar { char -> char.uppercase() } }
                val participant = Participant(
                    id = "p_${UUID.randomUUID().toString().take(8)}",
                    meetingId = meetingId,
                    name = name,
                    email = cleanEmail,
                    role = "Attendee",
                    rsvpStatus = RsvpStatus.PENDING,
                    isOrganizer = false,
                    avatarColor = listOf("#06B6D4", "#EC4899", "#10B981", "#8B5CF6", "#F59E0B").random()
                )
                meetingDao.insertParticipant(participant)
            }
        }

        // Add Agenda
        agendaTitles.forEachIndexed { index, agendaTitle ->
            if (agendaTitle.isNotBlank()) {
                val item = AgendaItem(
                    id = "ag_${UUID.randomUUID().toString().take(8)}",
                    meetingId = meetingId,
                    orderIndex = index + 1,
                    title = agendaTitle,
                    durationMinutes = (durationMinutes / agendaTitles.size).coerceAtLeast(5),
                    presenter = "Team"
                )
                meetingDao.insertAgendaItem(item)
            }
        }

        // Notification
        val notif = NotificationItem(
            id = "notif_${UUID.randomUUID().toString().take(8)}",
            title = "Meeting Scheduled",
            message = "'$title' scheduled for $date at $startTime.",
            type = NotificationType.INVITATION,
            relatedMeetingId = meetingId
        )
        meetingDao.insertNotification(notif)

        // Audit Log
        meetingDao.insertAuditLog(
            AuditLog(
                id = "log_${UUID.randomUUID().toString().take(8)}",
                action = "Meeting Created",
                actorName = user.name,
                actorRole = user.role.name,
                timestamp = "Just now",
                details = "Created '$title' with ${attendeeEmails.size + 1} participants",
                category = "Meeting"
            )
        )

        meetingId
    }

    suspend fun updateMeetingStatus(meetingId: String, status: MeetingStatus) = withContext(Dispatchers.IO) {
        val meeting = meetingDao.getMeetingByIdSync(meetingId)
        if (meeting != null) {
            meetingDao.updateMeeting(meeting.copy(status = status))
        }
    }

    suspend fun updateMeeting(meeting: Meeting) = withContext(Dispatchers.IO) {
        meetingDao.updateMeeting(meeting)
    }

    suspend fun deleteMeeting(meetingId: String) = withContext(Dispatchers.IO) {
        meetingDao.deleteMeetingById(meetingId)
    }

    suspend fun updateParticipantRsvp(participantId: String, status: RsvpStatus) = withContext(Dispatchers.IO) {
        // Find participant and update
    }

    suspend fun toggleAgendaItem(item: AgendaItem) = withContext(Dispatchers.IO) {
        meetingDao.updateAgendaItem(item.copy(isCompleted = !item.isCompleted))
    }

    suspend fun addNote(meetingId: String, text: String, isPrivate: Boolean, authorName: String) = withContext(Dispatchers.IO) {
        val note = MeetingNote(
            id = "n_${UUID.randomUUID().toString().take(8)}",
            meetingId = meetingId,
            authorName = authorName,
            text = text,
            isPrivate = isPrivate
        )
        meetingDao.insertNote(note)
    }

    suspend fun addTranscriptLine(meetingId: String, speaker: String, text: String, timeLabel: String, isDecision: Boolean = false, isAction: Boolean = false) = withContext(Dispatchers.IO) {
        val line = TranscriptLine(
            id = "tr_${UUID.randomUUID().toString().take(8)}",
            meetingId = meetingId,
            speaker = speaker,
            timeLabel = timeLabel,
            text = text,
            isDecision = isDecision,
            isActionItem = isAction
        )
        meetingDao.insertTranscriptLine(line)
    }

    suspend fun updateActionItemStatus(id: String, status: ActionItemStatus) = withContext(Dispatchers.IO) {
        meetingDao.updateActionItemStatus(id, status)
    }

    suspend fun addActionItem(item: ActionItem) = withContext(Dispatchers.IO) {
        meetingDao.insertActionItem(item)
    }

    suspend fun addDecision(decision: Decision) = withContext(Dispatchers.IO) {
        meetingDao.insertDecision(decision)
    }

    suspend fun markNotificationRead(id: String) = withContext(Dispatchers.IO) {
        meetingDao.markNotificationAsRead(id)
    }

    suspend fun markAllNotificationsRead() = withContext(Dispatchers.IO) {
        meetingDao.markAllNotificationsAsRead()
    }

    suspend fun sendChatMessage(meetingId: String, senderName: String, text: String) = withContext(Dispatchers.IO) {
        val msg = ChatMessage(
            id = "cm_${UUID.randomUUID().toString().take(8)}",
            meetingId = meetingId,
            senderName = senderName,
            text = text,
            timestamp = "Just now"
        )
        meetingDao.insertChatMessage(msg)
    }

    suspend fun updateUser(user: User) = withContext(Dispatchers.IO) {
        userDao.updateUser(user)
    }

    // --- AI Delegation ---
    suspend fun generateAgenda(title: String, meetingType: String, durationMinutes: Int): List<String> =
        GeminiRepository.generateAgenda(title, meetingType, durationMinutes)

    suspend fun generateBriefing(title: String, description: String, attendees: List<String>): String =
        GeminiRepository.generateBriefing(title, description, attendees)

    suspend fun generateSummaryAndExtracts(meetingId: String, meetingTitle: String, transcriptText: String): Triple<MeetingSummary, List<ActionItem>, List<Decision>> {
        val triple = GeminiRepository.generateSummaryAndExtracts(meetingId, meetingTitle, transcriptText)
        withContext(Dispatchers.IO) {
            meetingDao.insertSummary(triple.first)
            meetingDao.insertActionItems(triple.second)
            meetingDao.insertDecisions(triple.third)
            val meeting = meetingDao.getMeetingByIdSync(meetingId)
            if (meeting != null) {
                meetingDao.updateMeeting(meeting.copy(hasSummary = true, status = MeetingStatus.COMPLETED))
            }
        }
        return triple
    }

    suspend fun getSmartScheduleSuggestions(title: String, attendeeCount: Int, durationMinutes: Int): List<SmartTimeSlot> =
        GeminiRepository.getSmartScheduleSuggestions(title, attendeeCount, durationMinutes)

    suspend fun chatWithAi(query: String, contextInfo: String): String =
        GeminiRepository.chatWithAi(query, contextInfo)
}
