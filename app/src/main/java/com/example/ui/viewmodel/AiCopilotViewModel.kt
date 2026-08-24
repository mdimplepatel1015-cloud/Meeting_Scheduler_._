package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.remote.GeminiRepository
import com.example.data.repository.MeetingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

data class AiDialogMessage(
    val id: String = UUID.randomUUID().toString(),
    val text: String,
    val isUser: Boolean,
    val timestamp: String = "Just now",
    val suggestedAction: String? = null
)

class AiCopilotViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = MeetingRepository(database.meetingDao(), database.userDao())

    val isOpen = MutableStateFlow(false)
    val isThinking = MutableStateFlow(false)

    private val _messages = MutableStateFlow<List<AiDialogMessage>>(
        listOf(
            AiDialogMessage(
                text = "👋 Hello Alex! I'm your **MeetIQ AI Copilot**.\nI can help you find optimal meeting slots, draft agendas, extract action items, or summarize complex discussions. What can I do for you today?",
                isUser = false
            )
        )
    )
    val messages: StateFlow<List<AiDialogMessage>> = _messages.asStateFlow()

    val quickPrompts = listOf(
        "Find free time for tomorrow 📅",
        "Summarize last project meeting 📝",
        "What action items are overdue? ⚠️",
        "Create agenda for Client Review 🎯",
        "How is our team meeting health score? 💡"
    )

    fun toggleOpen(open: Boolean? = null) {
        isOpen.value = open ?: !isOpen.value
    }

    fun sendMessage(query: String) {
        if (query.isBlank()) return

        val userMsg = AiDialogMessage(text = query, isUser = true)
        _messages.value = _messages.value + userMsg
        isThinking.value = true

        viewModelScope.launch {
            try {
                val contextInfo = "Current user: Alex Morgan (Manager). 14 meetings this week. 1 overdue action item. Next meeting: Executive Launch Alignment at 2:00 PM."
                val aiResponseText = repository.chatWithAi(query, contextInfo)

                val aiMsg = AiDialogMessage(
                    text = aiResponseText,
                    isUser = false,
                    suggestedAction = when {
                        query.contains("schedule", ignoreCase = true) || query.contains("time", ignoreCase = true) -> "SCHEDULE_MEETING"
                        query.contains("agenda", ignoreCase = true) -> "CREATE_AGENDA"
                        query.contains("action", ignoreCase = true) || query.contains("overdue", ignoreCase = true) -> "VIEW_ACTIONS"
                        else -> null
                    }
                )
                _messages.value = _messages.value + aiMsg
            } finally {
                isThinking.value = false
            }
        }
    }

    fun clearChat() {
        _messages.value = listOf(
            AiDialogMessage(
                text = "Chat cleared. How can MeetIQ assist your meetings today?",
                isUser = false
            )
        )
    }
}
