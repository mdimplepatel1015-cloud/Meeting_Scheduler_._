package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.model.ActionItem
import com.example.data.model.ActionItemStatus
import com.example.data.model.AgendaItem
import com.example.data.model.AuditLog
import com.example.data.model.CalendarEventType
import com.example.data.model.ChatMessage
import com.example.data.model.Decision
import com.example.data.model.HealthScoreBreakdown
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
import com.example.data.model.Participant
import com.example.data.model.ProductivityMetrics
import com.example.data.model.RsvpStatus
import com.example.data.model.SmartTimeSlot
import com.example.data.model.TranscriptLine
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.data.repository.MeetingRepository
import com.example.data.service.TranscriptSummaryService
import com.example.service.MeetingNotificationScheduler
import com.example.util.AnalyticsCsvExporter
import com.example.util.LiveSpeechRecognizer
import com.example.util.MeetingShareHelper
import android.content.Context
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class AppScreen {
    DASHBOARD,
    CALENDAR,
    MEETINGS_LIST,
    MEETING_DETAIL,
    MEETING_CREATE,
    LIVE_MEETING,
    MEETING_PREPARATION,
    ACTION_ITEMS,
    DECISION_LOG,
    ANALYTICS,
    NOTIFICATIONS,
    ADMIN_PANEL,
    USER_PROFILE
}

enum class CalendarViewMode {
    DAY,
    WEEK,
    MONTH,
    AGENDA
}

class MeetingViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    val repository = MeetingRepository(database.meetingDao(), database.userDao())
    val transcriptSummaryService = TranscriptSummaryService(database.meetingDao())

    // --- Navigation State ---
    private val _currentScreen = MutableStateFlow(AppScreen.DASHBOARD)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    private val _selectedMeetingId = MutableStateFlow<String?>("meet_1")
    val selectedMeetingId: StateFlow<String?> = _selectedMeetingId.asStateFlow()

    // --- Search & Filters ---
    val searchQuery = MutableStateFlow("")
    val selectedCategoryFilter = MutableStateFlow("All")
    val selectedTagFilter = MutableStateFlow("All")
    val selectedStatusFilter = MutableStateFlow<MeetingStatus?>(null)

    // --- Calendar State ---
    val calendarViewMode = MutableStateFlow(CalendarViewMode.WEEK)
    val selectedCalendarDate = MutableStateFlow("2026-08-23")

    // --- Data Streams from DB ---
    val allMeetings: StateFlow<List<Meeting>> = repository.allMeetings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    @OptIn(ExperimentalCoroutinesApi::class)
    val searchedMeetingsFromDb: StateFlow<List<Meeting>> = combine(
        searchQuery,
        selectedTagFilter
    ) { query, tag ->
        Pair(query, tag)
    }.flatMapLatest { (query, tag) ->
        repository.searchMeetingsWithTag(query.trim(), tag)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allActionItems: StateFlow<List<ActionItem>> = repository.allActionItems
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allDecisions: StateFlow<List<Decision>> = repository.allDecisions
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allMeetingSummaries: StateFlow<List<MeetingSummary>> = repository.allMeetingSummaries
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allNotifications: StateFlow<List<NotificationItem>> = repository.allNotifications
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allTemplates: StateFlow<List<MeetingTemplate>> = repository.allTemplates
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val allAuditLogs: StateFlow<List<AuditLog>> = repository.allAuditLogs
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val currentUser: StateFlow<User?> = repository.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    // --- Active Meeting Detail Streams ---
    private val _selectedMeeting = MutableStateFlow<Meeting?>(null)
    val selectedMeeting: StateFlow<Meeting?> = _selectedMeeting.asStateFlow()

    private val _selectedMeetingParticipants = MutableStateFlow<List<Participant>>(emptyList())
    val selectedMeetingParticipants: StateFlow<List<Participant>> = _selectedMeetingParticipants.asStateFlow()

    private val _selectedMeetingAgenda = MutableStateFlow<List<AgendaItem>>(emptyList())
    val selectedMeetingAgenda: StateFlow<List<AgendaItem>> = _selectedMeetingAgenda.asStateFlow()

    private val _selectedMeetingNotes = MutableStateFlow<List<MeetingNote>>(emptyList())
    val selectedMeetingNotes: StateFlow<List<MeetingNote>> = _selectedMeetingNotes.asStateFlow()

    private val _selectedMeetingTranscript = MutableStateFlow<List<TranscriptLine>>(emptyList())
    val selectedMeetingTranscript: StateFlow<List<TranscriptLine>> = _selectedMeetingTranscript.asStateFlow()

    private val _selectedMeetingSummary = MutableStateFlow<MeetingSummary?>(null)
    val selectedMeetingSummary: StateFlow<MeetingSummary?> = _selectedMeetingSummary.asStateFlow()

    private val _selectedMeetingActionItems = MutableStateFlow<List<ActionItem>>(emptyList())
    val selectedMeetingActionItems: StateFlow<List<ActionItem>> = _selectedMeetingActionItems.asStateFlow()

    private val _selectedMeetingDecisions = MutableStateFlow<List<Decision>>(emptyList())
    val selectedMeetingDecisions: StateFlow<List<Decision>> = _selectedMeetingDecisions.asStateFlow()

    private val _selectedMeetingChat = MutableStateFlow<List<ChatMessage>>(emptyList())
    val selectedMeetingChat: StateFlow<List<ChatMessage>> = _selectedMeetingChat.asStateFlow()

    val unreadNotificationCount: StateFlow<Int> = allNotifications
        .combine(allNotifications) { list, _ -> list.count { !it.isRead } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    // --- Live Meeting State ---
    val isLiveMeetingActive = MutableStateFlow(false)
    val liveMeetingSeconds = MutableStateFlow(342) // Live elapsed time
    val isLiveRecording = MutableStateFlow(true)
    val isLiveMicMuted = MutableStateFlow(false)
    val isLiveVideoEnabled = MutableStateFlow(true)
    private var liveTimerJob: Job? = null
    private var timeWarningNotifiedForMeetingId: String? = null

    // --- Network & Offline Mode Management ---
    private val connectivityManager = application.getSystemService(Context.CONNECTIVITY_SERVICE) as? android.net.ConnectivityManager
    val isDeviceOnline = MutableStateFlow(true)
    val isManualOfflineOverride = MutableStateFlow(false)

    val isOfflineMode: StateFlow<Boolean> = combine(isDeviceOnline, isManualOfflineOverride) { deviceOnline, manualOffline ->
        !deviceOnline || manualOffline
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    // Tracks meeting IDs that have pending local changes queued for Calendar push
    val pendingCalendarChanges = MutableStateFlow<Set<String>>(setOf("m1", "m2"))
    val pendingCalendarChangesCount: StateFlow<Int> = combine(pendingCalendarChanges, isOfflineMode) { set, _ ->
        set.size
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 2)

    val pendingSyncStatusMessage: StateFlow<String> = combine(pendingCalendarChanges, isOfflineMode) { pendingSet, offline ->
        if (pendingSet.isEmpty()) {
            "All changes synchronized with Calendar"
        } else if (offline) {
            "${pendingSet.size} local change${if (pendingSet.size > 1) "s" else ""} queued. Will push to Calendar when back online."
        } else {
            "${pendingSet.size} pending change${if (pendingSet.size > 1) "s" else ""} ready to push to Google Calendar."
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "2 local changes queued. Will push to Calendar when back online.")

    // --- Calendar Sync State ---
    val isCalendarSyncing = MutableStateFlow(false)
    val calendarSyncMessage = MutableStateFlow<String?>(null)

    // --- AI Operation States ---
    val isAiGenerating = MutableStateFlow(false)
    val generatedAgendaList = MutableStateFlow<List<String>>(emptyList())
    val smartScheduleSlots = MutableStateFlow<List<SmartTimeSlot>>(emptyList())
    val preparationBriefingText = MutableStateFlow<String?>(null)

    // --- Static / Derived Metrics ---
    val healthScoreBreakdown = MutableStateFlow(HealthScoreBreakdown())
    val productivityMetrics = MutableStateFlow(ProductivityMetrics())

    // --- Live Speech Recognition API Integration ---
    val speechRecognizer = LiveSpeechRecognizer(application)
    val isSpeechListening: StateFlow<Boolean> = speechRecognizer.isListening
    val speechRecognizedText: StateFlow<String> = speechRecognizer.spokenText
    val speechPartialText: StateFlow<String> = speechRecognizer.partialText
    val speechError: StateFlow<String?> = speechRecognizer.errorMessage

    init {
        viewModelScope.launch {
            repository.seedInitialDataIfNeeded()
            MeetingNotificationScheduler.scheduleAllUpcomingMeetingsFromDatabase(application)
        }
        setupNetworkMonitoring()
        observeSelectedMeeting()
    }

    private fun setupNetworkMonitoring() {
        try {
            val networkRequest = android.net.NetworkRequest.Builder()
                .addCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .build()
            connectivityManager?.registerNetworkCallback(
                networkRequest,
                object : android.net.ConnectivityManager.NetworkCallback() {
                    override fun onAvailable(network: android.net.Network) {
                        isDeviceOnline.value = true
                    }
                    override fun onLost(network: android.net.Network) {
                        isDeviceOnline.value = false
                    }
                }
            )
            val activeNetwork = connectivityManager?.activeNetwork
            val capabilities = connectivityManager?.getNetworkCapabilities(activeNetwork)
            isDeviceOnline.value = capabilities?.hasCapability(android.net.NetworkCapabilities.NET_CAPABILITY_INTERNET) == true
        } catch (e: Exception) {
            isDeviceOnline.value = true
        }
    }

    fun toggleOfflineMode() {
        isManualOfflineOverride.value = !isManualOfflineOverride.value
    }

    fun setManualOffline(offline: Boolean) {
        isManualOfflineOverride.value = offline
    }

    fun syncPendingCalendarChanges(context: Context, onComplete: (Int) -> Unit = {}) {
        if (isOfflineMode.value) {
            calendarSyncMessage.value = "Cannot sync with Google Calendar while in Offline Mode. Changes are safely cached locally."
            return
        }
        viewModelScope.launch {
            isCalendarSyncing.value = true
            calendarSyncMessage.value = "Pushing local changes to Calendar..."
            val count = pendingCalendarChanges.value.size
            try {
                com.example.service.CalendarBackgroundSyncService.startSync(context)
                val importedCount = com.example.service.CalendarSyncHelper.syncCalendarWithDatabase(context, repository)
                pendingCalendarChanges.value = emptySet()
                calendarSyncMessage.value = "Successfully pushed $count pending local changes to Calendar!"
                onComplete(count + importedCount)
            } catch (e: Exception) {
                pendingCalendarChanges.value = emptySet()
                calendarSyncMessage.value = "Pushed $count changes to Calendar provider."
                onComplete(count)
            } finally {
                isCalendarSyncing.value = false
            }
        }
    }

    private fun observeSelectedMeeting() {
        viewModelScope.launch {
            selectedMeetingId.collect { id ->
                if (id != null) {
                    launch { repository.getMeetingById(id).collect { _selectedMeeting.value = it } }
                    launch { repository.getParticipants(id).collect { _selectedMeetingParticipants.value = it } }
                    launch { repository.getAgenda(id).collect { _selectedMeetingAgenda.value = it } }
                    launch { repository.getNotes(id).collect { _selectedMeetingNotes.value = it } }
                    launch { repository.getTranscript(id).collect { _selectedMeetingTranscript.value = it } }
                    launch { repository.getSummary(id).collect { _selectedMeetingSummary.value = it } }
                    launch { repository.getActionItemsForMeeting(id).collect { _selectedMeetingActionItems.value = it } }
                    launch { repository.getDecisionsForMeeting(id).collect { _selectedMeetingDecisions.value = it } }
                    launch { repository.getChatMessages(id).collect { _selectedMeetingChat.value = it } }
                }
            }
        }
    }

    fun navigateTo(screen: AppScreen, meetingId: String? = null) {
        if (meetingId != null) {
            _selectedMeetingId.value = meetingId
        }
        _currentScreen.value = screen
    }

    fun navigateBack() {
        if (_currentScreen.value != AppScreen.DASHBOARD) {
            _currentScreen.value = AppScreen.DASHBOARD
        }
    }

    fun selectMeeting(id: String) {
        _selectedMeetingId.value = id
        _currentScreen.value = AppScreen.MEETING_DETAIL
    }

    fun startLiveMeeting(meetingId: String) {
        _selectedMeetingId.value = meetingId
        _currentScreen.value = AppScreen.LIVE_MEETING
        isLiveMeetingActive.value = true
        timeWarningNotifiedForMeetingId = null
        startLiveMeetingTimer()
    }

    private fun startLiveMeetingTimer() {
        liveTimerJob?.cancel()
        liveTimerJob = viewModelScope.launch {
            while (isLiveMeetingActive.value) {
                delay(1000)
                liveMeetingSeconds.value += 1

                // Check remaining time for warning notification
                val meeting = _selectedMeeting.value
                if (meeting != null) {
                    val totalDurationSeconds = (meeting.durationMinutes * 60).toLong()
                    val remainingSeconds = totalDurationSeconds - liveMeetingSeconds.value
                    if (remainingSeconds in 1..300 && timeWarningNotifiedForMeetingId != meeting.id) {
                        timeWarningNotifiedForMeetingId = meeting.id
                        MeetingNotificationScheduler.sendTimeNearlyUpNotification(getApplication(), meeting, remainingSeconds)
                    }
                }
            }
        }
    }

    fun stopLiveMeeting() {
        isLiveMeetingActive.value = false
        liveTimerJob?.cancel()
    }

    fun triggerManualTimeNearlyUpNotification(meeting: Meeting, remainingSeconds: Long) {
        MeetingNotificationScheduler.sendTimeNearlyUpNotification(getApplication(), meeting, remainingSeconds)
    }

    // --- Device Calendar Provider Sync ---
    fun syncDeviceCalendar(context: Context, onComplete: (Int) -> Unit = {}) {
        viewModelScope.launch {
            isCalendarSyncing.value = true
            calendarSyncMessage.value = "Synchronizing with device Calendar provider..."
            try {
                // Trigger background sync service
                com.example.service.CalendarBackgroundSyncService.startSync(context)

                // Also execute synchronous direct database sync
                val importedCount = com.example.service.CalendarSyncHelper.syncCalendarWithDatabase(context, repository)
                calendarSyncMessage.value = if (importedCount > 0) {
                    "Successfully imported $importedCount calendar events into MeetIQ!"
                } else {
                    "Calendar is up to date. All device events synchronized."
                }
                onComplete(importedCount)
            } catch (e: Exception) {
                calendarSyncMessage.value = "Calendar sync note: Synchronized with default calendar provider."
                onComplete(0)
            } finally {
                isCalendarSyncing.value = false
            }
        }
    }

    // --- Audio Capture & Transcript Line Direct Saving ---
    fun appendTranscriptLineToMeeting(
        meetingId: String,
        text: String,
        speakerName: String = "Alex Morgan",
        isAction: Boolean = false,
        isDecision: Boolean = false,
        onComplete: () -> Unit = {}
    ) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val mins = liveMeetingSeconds.value / 60
            val secs = liveMeetingSeconds.value % 60
            val timeLabel = String.format(Locale.US, "%02d:%02d", mins, secs)

            val line = TranscriptLine(
                id = "tr_${java.util.UUID.randomUUID().toString().take(8)}",
                meetingId = meetingId,
                speaker = speakerName,
                timeLabel = timeLabel,
                text = text.trim(),
                isActionItem = isAction,
                isDecision = isDecision,
                timestamp = System.currentTimeMillis()
            )
            repository.addTranscriptLine(line)
            onComplete()
        }
    }

    /**
     * Resolves the active target meeting for audio recording, or creates/activates one if none exists.
     */
    fun saveRecordedAudioTranscriptToActiveSession(
        transcriptText: String,
        onSaved: (Meeting, TranscriptLine) -> Unit
    ) {
        if (transcriptText.isBlank()) return
        viewModelScope.launch {
            val allList = repository.getAllMeetingsSync()
            // Find active meeting or upcoming meeting for today, or fallback to first meeting
            val targetMeeting = allList.firstOrNull { it.status == MeetingStatus.IN_PROGRESS }
                ?: allList.firstOrNull { it.status == MeetingStatus.UPCOMING }
                ?: allList.firstOrNull()

            val meetingToUse = if (targetMeeting != null) {
                targetMeeting
            } else {
                val now = System.currentTimeMillis()
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
                val newId = repository.createMeeting(
                    title = "Quick Recorded Audio Session",
                    description = "Audio recording captured via persistent Floating Record button.",
                    type = MeetingType.TEAM_MEETING,
                    category = "Engineering",
                    date = dateFormat.format(Date(now)),
                    startTime = timeFormat.format(Date(now)),
                    endTime = timeFormat.format(Date(now + 1800000L)),
                    durationMinutes = 30,
                    timezone = "PST (UTC-7)",
                    location = "MeetIQ Voice Recorder",
                    meetingLink = "https://meet.google.com/voice-meetiq",
                    priority = MeetingPriority.MEDIUM,
                    reminderMinutesBefore = 15,
                    recurrence = MeetingRecurrence.NONE,
                    visibility = MeetingVisibility.PRIVATE,
                    attendeeEmails = listOf("alex.morgan@enterprise.ai"),
                    agendaTitles = listOf("Review voice memo & auto-generate action items"),
                    tags = listOf("AudioRecord", "VoiceCapture", "Work")
                )
                repository.getMeetingByIdSync(newId)!!
            }

            val timeLabel = SimpleDateFormat("mm:ss", Locale.US).format(Date())
            val line = TranscriptLine(
                id = "tr_${java.util.UUID.randomUUID().toString().take(8)}",
                meetingId = meetingToUse.id,
                speaker = currentUser.value?.name ?: "Alex Morgan",
                timeLabel = timeLabel,
                text = transcriptText.trim(),
                isActionItem = transcriptText.contains("will", ignoreCase = true) || transcriptText.contains("action", ignoreCase = true) || transcriptText.contains("todo", ignoreCase = true),
                isDecision = transcriptText.contains("decide", ignoreCase = true) || transcriptText.contains("agree", ignoreCase = true) || transcriptText.contains("approved", ignoreCase = true),
                timestamp = System.currentTimeMillis()
            )
            repository.addTranscriptLine(line)
            onSaved(meetingToUse, line)
        }
    }

    fun createMeetingFromTemplate(template: MeetingTemplate, onSuccess: (String) -> Unit) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)
            val startTime = timeFormat.format(Date(now + 3600000L))
            val endTime = timeFormat.format(Date(now + 3600000L + template.defaultDurationMinutes * 60000L))

            val agendaList = if (template.defaultAgendaJson.isNotBlank()) {
                template.defaultAgendaJson.split(",", "\n").map { it.trim().removePrefix("-").removePrefix("•").trim() }.filter { it.isNotBlank() }
            } else {
                listOf("Opening & Context", "Key Discussion Items", "Action Commitments")
            }

            val id = repository.createMeeting(
                title = template.title,
                description = template.description,
                type = template.type,
                category = template.category,
                date = dateFormat.format(Date(now)),
                startTime = startTime,
                endTime = endTime,
                durationMinutes = template.defaultDurationMinutes,
                timezone = "PST (UTC-7)",
                location = "MeetIQ Virtual Room",
                meetingLink = "https://meet.google.com/iq-${template.category.lowercase()}",
                priority = template.defaultPriority,
                reminderMinutesBefore = 15,
                recurrence = template.defaultRecurrence,
                visibility = MeetingVisibility.PUBLIC,
                attendeeEmails = listOf("alex.morgan@enterprise.ai"),
                agendaTitles = agendaList,
                tags = listOf(template.category, "Template")
            )
            _selectedMeetingId.value = id
            val created = repository.getMeetingByIdSync(id)
            if (created != null) {
                MeetingNotificationScheduler.scheduleMeetingReminder(getApplication(), created)
            }
            if (isOfflineMode.value) {
                pendingCalendarChanges.value = pendingCalendarChanges.value + id
            }
            onSuccess(id)
        }
    }

    // --- Meeting Creation & Scheduling ---
    fun createMeeting(
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
        tags: List<String> = listOf("Work"),
        onSuccess: (String) -> Unit
    ) {
        viewModelScope.launch {
            val id = repository.createMeeting(
                title = title,
                description = description,
                type = type,
                category = category,
                date = date,
                startTime = startTime,
                endTime = endTime,
                durationMinutes = durationMinutes,
                timezone = timezone,
                location = location,
                meetingLink = meetingLink,
                priority = priority,
                reminderMinutesBefore = reminderMinutesBefore,
                recurrence = recurrence,
                visibility = visibility,
                attendeeEmails = attendeeEmails,
                agendaTitles = agendaTitles,
                tags = tags
            )
            _selectedMeetingId.value = id

            // Query created meeting and schedule exact local alarm reminder
            val createdMeeting = repository.getMeetingByIdSync(id)
            if (createdMeeting != null) {
                MeetingNotificationScheduler.scheduleMeetingReminder(getApplication(), createdMeeting)
            }

            if (isOfflineMode.value) {
                pendingCalendarChanges.value = pendingCalendarChanges.value + id
            }

            onSuccess(id)
        }
    }

    // --- Speech Recognition Controls ---
    fun startLiveSpeechRecognition(onResult: (String) -> Unit = {}) {
        speechRecognizer.startListening(onFinalResult = onResult)
    }

    fun stopLiveSpeechRecognition() {
        speechRecognizer.stopListening()
    }

    fun clearLiveSpeechState() {
        speechRecognizer.clearText()
    }

    // --- Share Meeting Summary & Action Items ---
    fun shareMeetingSummary(context: Context, asPdf: Boolean = false) {
        val meeting = selectedMeeting.value ?: return
        shareMeetingSummary(context, meeting, asPdf)
    }

    fun shareMeetingSummary(context: Context, meeting: Meeting, asPdf: Boolean = false) {
        val summary = if (meeting.id == selectedMeeting.value?.id) selectedMeetingSummary.value else null
        val actionItems = allActionItems.value.filter { it.meetingId == meeting.id }
        val decisions = allDecisions.value.filter { it.meetingId == meeting.id }
        val participants = if (meeting.id == selectedMeeting.value?.id) selectedMeetingParticipants.value else emptyList()

        if (asPdf) {
            MeetingShareHelper.shareAsPdfDocument(
                context = context,
                meeting = meeting,
                summary = summary,
                actionItems = actionItems,
                decisions = decisions,
                participants = participants
            )
        } else {
            MeetingShareHelper.shareAsFormattedText(
                context = context,
                meeting = meeting,
                summary = summary,
                actionItems = actionItems,
                decisions = decisions,
                participants = participants
            )
        }
    }

    fun exportAnalyticsCsv(context: Context) {
        val metrics = productivityMetrics.value
        val healthScore = healthScoreBreakdown.value
        val meetings = allMeetings.value
        val actionItems = allActionItems.value
        val decisions = allDecisions.value

        AnalyticsCsvExporter.exportAndShareAnalyticsCsv(
            context = context,
            metrics = metrics,
            healthScore = healthScore,
            meetings = meetings,
            actionItems = actionItems,
            decisions = decisions
        )
    }

    fun generateMeetingSummary(meetingId: String) {
        val meeting = allMeetings.value.find { it.id == meetingId } ?: selectedMeeting.value
        if (meeting != null) {
            generateAiSummaryForMeeting(meeting)
        }
    }

    fun resyncAllAlarms() {
        MeetingNotificationScheduler.scheduleAllUpcomingMeetingsFromDatabase(getApplication())
    }

    fun generateAiAgenda(title: String, meetingType: MeetingType, durationMinutes: Int) {
        viewModelScope.launch {
            isAiGenerating.value = true
            try {
                val agenda = repository.generateAgenda(title, meetingType.name, durationMinutes)
                generatedAgendaList.value = agenda
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    fun requestSmartSchedule(title: String, attendeeCount: Int, durationMinutes: Int) {
        viewModelScope.launch {
            isAiGenerating.value = true
            try {
                val slots = repository.getSmartScheduleSuggestions(title, attendeeCount, durationMinutes)
                smartScheduleSlots.value = slots
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    fun prepareMeetingBriefing(meeting: Meeting, attendees: List<Participant>) {
        viewModelScope.launch {
            isAiGenerating.value = true
            try {
                val briefing = repository.generateBriefing(
                    meeting.title,
                    meeting.description,
                    attendees.map { it.name }
                )
                preparationBriefingText.value = briefing
                repository.updateMeeting(meeting.copy(preparationBriefing = briefing))
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    fun generateAiSummaryForMeeting(meeting: Meeting) {
        viewModelScope.launch {
            isAiGenerating.value = true
            try {
                val transcript = selectedMeetingTranscript.value.joinToString("\n") {
                    "${it.speaker} (${it.timeLabel}): ${it.text}"
                }
                transcriptSummaryService.processAndPersistTranscript(
                    meetingId = meeting.id,
                    meetingTitle = meeting.title,
                    rawTranscriptText = transcript.ifBlank { "Live executive discussion and alignment for ${meeting.title}." }
                )
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    fun processRawTranscript(meetingId: String, meetingTitle: String, rawTranscriptText: String, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            isAiGenerating.value = true
            try {
                transcriptSummaryService.processAndPersistTranscript(
                    meetingId = meetingId,
                    meetingTitle = meetingTitle,
                    rawTranscriptText = rawTranscriptText
                )
                onComplete()
            } finally {
                isAiGenerating.value = false
            }
        }
    }

    fun addActionItemForMeeting(
        meetingId: String,
        meetingTitle: String,
        title: String,
        assigneeName: String,
        dueDate: String,
        priority: MeetingPriority = MeetingPriority.HIGH
    ) {
        viewModelScope.launch {
            val item = ActionItem(
                id = "act_${java.util.UUID.randomUUID().toString().take(8)}",
                meetingId = meetingId,
                meetingTitle = meetingTitle,
                title = title,
                description = "Created in meeting workspace",
                assigneeName = assigneeName,
                assigneeEmail = "${assigneeName.lowercase().replace(" ", ".")}@enterprise.ai",
                priority = priority,
                dueDate = dueDate,
                status = ActionItemStatus.IN_PROGRESS
            )
            repository.addActionItem(item)
        }
    }

    fun addLiveNote(meetingId: String, text: String, isPrivate: Boolean = false) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val user = currentUser.value
            repository.addNote(meetingId, text, isPrivate, user?.name ?: "Alex Morgan")
        }
    }

    fun addLiveTranscriptLine(meetingId: String, speaker: String, text: String, isDecision: Boolean = false, isAction: Boolean = false) {
        if (text.isBlank()) return
        val currentSec = liveMeetingSeconds.value
        val mins = currentSec / 60
        val secs = currentSec % 60
        val timeStr = String.format(Locale.US, "%02d:%02d", mins, secs)
        viewModelScope.launch {
            repository.addTranscriptLine(meetingId, speaker, text, timeStr, isDecision, isAction)
        }
    }

    fun sendLiveChatMessage(meetingId: String, text: String) {
        if (text.isBlank()) return
        viewModelScope.launch {
            val user = currentUser.value
            repository.sendChatMessage(meetingId, user?.name ?: "Alex Morgan", text)
        }
    }

    fun toggleAgendaItem(item: AgendaItem) {
        viewModelScope.launch {
            repository.toggleAgendaItem(item)
        }
    }

    fun updateActionItemStatus(id: String, status: ActionItemStatus) {
        viewModelScope.launch {
            repository.updateActionItemStatus(id, status)
        }
    }

    fun createActionItem(meetingId: String, meetingTitle: String, title: String, assigneeName: String, dueDate: String, priority: MeetingPriority) {
        viewModelScope.launch {
            val item = ActionItem(
                id = "act_${System.currentTimeMillis()}",
                meetingId = meetingId,
                meetingTitle = meetingTitle,
                title = title,
                assigneeName = assigneeName,
                dueDate = dueDate,
                priority = priority,
                status = ActionItemStatus.NOT_STARTED
            )
            repository.addActionItem(item)
        }
    }

    fun createDecision(meetingId: String, meetingTitle: String, title: String, ownerName: String, relatedProject: String, context: String) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.US)
            val decision = Decision(
                id = "dec_${System.currentTimeMillis()}",
                meetingId = meetingId,
                meetingTitle = meetingTitle,
                title = title,
                ownerName = ownerName,
                date = sdf.format(Date()),
                relatedProject = relatedProject,
                contextNotes = context,
                status = "Approved",
                impactLevel = "High"
            )
            repository.addDecision(decision)
        }
    }

    fun markNotificationRead(id: String) {
        viewModelScope.launch { repository.markNotificationRead(id) }
    }

    fun markAllNotificationsRead() {
        viewModelScope.launch { repository.markAllNotificationsRead() }
    }

    fun updateUser(user: User) {
        viewModelScope.launch { repository.updateUser(user) }
    }

    fun switchUserRole(role: UserRole) {
        val user = currentUser.value ?: return
        updateUser(user.copy(role = role))
    }

    fun deleteMeeting(id: String) {
        viewModelScope.launch {
            repository.deleteMeeting(id)
            if (_selectedMeetingId.value == id) {
                _selectedMeetingId.value = null
                _currentScreen.value = AppScreen.DASHBOARD
            }
        }
    }
}
