package com.example.ui.screens

import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.Meeting
import com.example.data.model.MeetingStatus
import com.example.data.model.MeetingTemplate
import com.example.ui.components.CreateMeetingBottomSheet
import com.example.ui.components.MeetingCard
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseError
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MeetingViewModel

@Composable
fun MeetingsListScreen(
    viewModel: MeetingViewModel
) {
    val context = LocalContext.current
    val meetingsFromDb by viewModel.searchedMeetingsFromDb.collectAsState()
    val allSummaries by viewModel.allMeetingSummaries.collectAsState()
    val summaryMap = remember(allSummaries) { allSummaries.associateBy { it.meetingId } }

    val templates by viewModel.allTemplates.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val selectedCategory by viewModel.selectedCategoryFilter.collectAsState()
    val selectedTag by viewModel.selectedTagFilter.collectAsState()
    val selectedStatus by viewModel.selectedStatusFilter.collectAsState()
    val isCalendarSyncing by viewModel.isCalendarSyncing.collectAsState()
    val calendarSyncMessage by viewModel.calendarSyncMessage.collectAsState()

    var isCreateSheetOpen by remember { mutableStateOf(false) }
    var isRecordingDialogOpen by remember { mutableStateOf(false) }
    var selectedTargetMeetingForRecord by remember { mutableStateOf<Meeting?>(null) }
    var manualTranscriptInput by remember { mutableStateOf("") }

    val isListening by viewModel.isSpeechListening.collectAsState()
    val spokenText by viewModel.speechRecognizedText.collectAsState()
    val partialText by viewModel.speechPartialText.collectAsState()

    val categories = listOf("All", "Engineering", "Product", "People", "DevOps", "Sales")
    val tags = listOf("All", "Work", "Personal", "Client", "Engineering", "Architecture", "Strategy", "Product", "Executive", "Design")

    // Calendar Permission Launcher
    val calendarPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            viewModel.syncDeviceCalendar(context) { count ->
                Toast.makeText(context, "Synchronized $count events from device Calendar!", Toast.LENGTH_SHORT).show()
            }
        } else {
            viewModel.syncDeviceCalendar(context) {
                Toast.makeText(context, "Synchronized with default Calendar provider.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Audio Recording Permission Launcher
    val recordAudioLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            isRecordingDialogOpen = true
            val activeMeeting = meetingsFromDb.firstOrNull { it.status == MeetingStatus.IN_PROGRESS }
                ?: meetingsFromDb.firstOrNull { it.status == MeetingStatus.UPCOMING }
                ?: meetingsFromDb.firstOrNull()
            selectedTargetMeetingForRecord = activeMeeting
            viewModel.startLiveSpeechRecognition()
        } else {
            Toast.makeText(context, "Microphone permission required for meeting audio recording.", Toast.LENGTH_SHORT).show()
        }
    }

    val filteredMeetings = meetingsFromDb.filter { meeting ->
        val matchesCategory = selectedCategory == "All" || meeting.category.equals(selectedCategory, ignoreCase = true)
        val matchesStatus = selectedStatus == null || meeting.status == selectedStatus
        matchesCategory && matchesStatus
    }

    // Flashing Recording Animation for FAB
    val infiniteTransition = rememberInfiniteTransition(label = "recordingFabPulse")
    val fabAlpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "fabPulse"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("meetings_list_screen_root"),
            verticalArrangement = Arrangement.spacedBy(14.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 96.dp)
        ) {
            // Header with Quick Schedule & Calendar Sync
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Scheduled Meetings",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Timeline, agendas & attendee status",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        // Background Calendar Sync Button
                        OutlinedButton(
                            onClick = {
                                val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED
                                if (hasPerm) {
                                    viewModel.syncDeviceCalendar(context) { count ->
                                        Toast.makeText(context, "Imported $count calendar events into MeetIQ!", Toast.LENGTH_SHORT).show()
                                    }
                                } else {
                                    calendarPermissionLauncher.launch(Manifest.permission.READ_CALENDAR)
                                }
                            },
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("sync_calendar_button")
                        ) {
                            if (isCalendarSyncing) {
                                CircularProgressIndicator(modifier = Modifier.size(14.dp), strokeWidth = 2.dp, color = CyanAccent)
                            } else {
                                Icon(Icons.Default.Sync, contentDescription = "Sync Calendar", modifier = Modifier.size(14.dp), tint = CyanAccent)
                            }
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sync", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = CyanAccent)
                        }

                        Button(
                            onClick = { isCreateSheetOpen = true },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.testTag("open_create_meeting_modal_btn")
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("+ New", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Sync status banner if recently triggered
            if (!calendarSyncMessage.isNullOrBlank()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .testTag("calendar_sync_status_banner"),
                        color = CyanAccent.copy(alpha = 0.12f)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = calendarSyncMessage ?: "",
                                    fontSize = 11.sp,
                                    color = CyanAccent,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss",
                                tint = CyanAccent,
                                modifier = Modifier
                                    .size(14.dp)
                                    .clickable { viewModel.calendarSyncMessage.value = null }
                            )
                        }
                    }
                }
            }

            // Search Input
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { viewModel.searchQuery.value = it },
                    placeholder = { Text("Search meetings, agendas, categories...", fontSize = 13.sp) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("meetings_search_input"),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = IndigoLight,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    ),
                    singleLine = true
                )
            }

            // Filter Chips: Categories
            item {
                Column {
                    Text(
                        text = "Categories",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(categories) { category ->
                            val isSelected = selectedCategory.equals(category, ignoreCase = true)
                            Surface(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(20.dp))
                                    .clickable { viewModel.selectedCategoryFilter.value = category }
                                    .testTag("category_filter_${category.lowercase()}"),
                                color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                            ) {
                                Text(
                                    text = category,
                                    style = MaterialTheme.typography.labelMedium,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                        }
                    }
                }
            }

            // Status Filter Tabs
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val statusOptions = listOf(
                        "All Status" to null,
                        "Upcoming" to MeetingStatus.UPCOMING,
                        "Completed" to MeetingStatus.COMPLETED
                    )
                    statusOptions.forEach { (label, status) ->
                        val isSelected = selectedStatus == status
                        Surface(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(10.dp))
                                .border(
                                    1.dp,
                                    if (isSelected) CyanAccent else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    RoundedCornerShape(10.dp)
                                )
                                .clickable { viewModel.selectedStatusFilter.value = status }
                                .testTag("status_filter_${label.lowercase().replace(" ", "_")}"),
                            color = if (isSelected) CyanAccent.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ) {
                            Box(
                                modifier = Modifier.padding(vertical = 8.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSelected) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Templates Quick Launch Section
            if (templates.isNotEmpty()) {
                item {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Quick Templates",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Use structured agendas",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(templates) { template ->
                                TemplateChip(
                                    template = template,
                                    onClick = {
                                        viewModel.createMeetingFromTemplate(template) { newId ->
                                            viewModel.selectMeeting(newId)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Meeting List Items with Summary Preview Card
            if (filteredMeetings.isEmpty()) {
                item {
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 32.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FilterList,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = "No meetings match your criteria",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Try adjusting your search query or category filters",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    viewModel.searchQuery.value = ""
                                    viewModel.selectedCategoryFilter.value = "All"
                                    viewModel.selectedTagFilter.value = "All"
                                    viewModel.selectedStatusFilter.value = null
                                }
                            ) {
                                Text("Reset Filters")
                            }
                        }
                    }
                }
            } else {
                items(filteredMeetings) { meeting ->
                    MeetingCard(
                        meeting = meeting,
                        summary = summaryMap[meeting.id],
                        onClick = { viewModel.selectMeeting(meeting.id) },
                        onJoinLive = { viewModel.startLiveMeeting(meeting.id) },
                        onPrepare = { viewModel.navigateTo(AppScreen.MEETING_PREPARATION, meeting.id) }
                    )
                }
            }
        }

        // Persistent 'Record' Floating Action Button (FAB)
        ExtendedFloatingActionButton(
            onClick = {
                val hasPerm = ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                if (hasPerm) {
                    isRecordingDialogOpen = true
                    val activeMeeting = meetingsFromDb.firstOrNull { it.status == MeetingStatus.IN_PROGRESS }
                        ?: meetingsFromDb.firstOrNull { it.status == MeetingStatus.UPCOMING }
                        ?: meetingsFromDb.firstOrNull()
                    selectedTargetMeetingForRecord = activeMeeting
                    viewModel.startLiveSpeechRecognition()
                } else {
                    recordAudioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 20.dp)
                .testTag("meetings_record_fab"),
            containerColor = if (isListening) RoseError else IndigoPrimary,
            contentColor = Color.White,
            shape = RoundedCornerShape(28.dp),
            icon = {
                if (isListening) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = "Recording Active",
                        tint = Color.White.copy(alpha = fabAlpha),
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Record Meeting Audio",
                        modifier = Modifier.size(20.dp)
                    )
                }
            },
            text = {
                Text(
                    text = if (isListening) "REC • Capturing" else "Record Audio",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        )

        // Floating Audio Recording & Transcript Capture Dialog
        if (isRecordingDialogOpen) {
            AlertDialog(
                onDismissRequest = {
                    viewModel.stopLiveSpeechRecognition()
                    isRecordingDialogOpen = false
                },
                title = {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.FiberManualRecord,
                                contentDescription = "Recording",
                                tint = RoseError.copy(alpha = if (isListening) fabAlpha else 0.4f),
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Live Audio Capture",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        // Target Active Meeting Tag
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp)),
                            color = IndigoPrimary.copy(alpha = 0.12f)
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                Text(
                                    text = "TARGET ACTIVE SESSION",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = IndigoLight
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = selectedTargetMeetingForRecord?.title ?: "Quick Audio Capture Meeting",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Real-time speech recognition preview
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(120.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .border(1.dp, if (isListening) CyanAccent.copy(alpha = 0.5f) else MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(10.dp)),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.GraphicEq,
                                            contentDescription = null,
                                            tint = if (isListening) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.size(14.dp)
                                        )
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = if (isListening) "Listening to microphone..." else "Microphone ready",
                                            fontSize = 10.sp,
                                            color = if (isListening) CyanAccent else MaterialTheme.colorScheme.onSurfaceVariant,
                                            fontWeight = FontWeight.Medium
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                val displayedSpeech = when {
                                    spokenText.isNotBlank() -> spokenText
                                    partialText.isNotBlank() -> partialText
                                    else -> "Speak into your microphone. Captured audio will be transcribed here in real-time and saved to this meeting's session..."
                                }

                                Text(
                                    text = displayedSpeech,
                                    fontSize = 12.sp,
                                    color = if (spokenText.isNotBlank() || partialText.isNotBlank()) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        // Manual quick note / transcription input
                        OutlinedTextField(
                            value = manualTranscriptInput,
                            onValueChange = { manualTranscriptInput = it },
                            placeholder = { Text("Or type a transcript note directly...", fontSize = 11.sp) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("record_dialog_manual_input"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            val textToSave = when {
                                manualTranscriptInput.isNotBlank() -> manualTranscriptInput
                                spokenText.isNotBlank() -> spokenText
                                partialText.isNotBlank() -> partialText
                                else -> "Captured team discussion point and action commitments."
                            }

                            val target = selectedTargetMeetingForRecord
                            if (target != null) {
                                viewModel.appendTranscriptLineToMeeting(
                                    meetingId = target.id,
                                    text = textToSave,
                                    onComplete = {
                                        Toast.makeText(context, "Saved transcript to '${target.title}'", Toast.LENGTH_SHORT).show()
                                    }
                                )
                            } else {
                                viewModel.saveRecordedAudioTranscriptToActiveSession(textToSave) { m, _ ->
                                    Toast.makeText(context, "Saved transcript to '${m.title}'", Toast.LENGTH_SHORT).show()
                                }
                            }
                            viewModel.stopLiveSpeechRecognition()
                            viewModel.clearLiveSpeechState()
                            manualTranscriptInput = ""
                            isRecordingDialogOpen = false
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        modifier = Modifier.testTag("save_recorded_transcript_button")
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Save to Session")
                    }
                },
                dismissButton = {
                    OutlinedButton(
                        onClick = {
                            viewModel.stopLiveSpeechRecognition()
                            viewModel.clearLiveSpeechState()
                            isRecordingDialogOpen = false
                        }
                    ) {
                        Text("Cancel")
                    }
                }
            )
        }

        // Quick Meeting Creation Bottom Sheet Modal
        CreateMeetingBottomSheet(
            isOpen = isCreateSheetOpen,
            onDismiss = { isCreateSheetOpen = false },
            viewModel = viewModel
        )
    }
}

@Composable
private fun TemplateChip(
    template: MeetingTemplate,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .width(160.dp)
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag("template_chip_${template.id}"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(IndigoPrimary.copy(alpha = 0.2f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = "${template.defaultDurationMinutes}m",
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoLight
                    )
                }
                Text(
                    text = template.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = template.title,
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
        }
    }
}
