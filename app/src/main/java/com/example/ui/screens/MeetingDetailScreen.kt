package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionItemStatus
import com.example.data.model.MeetingPriority
import com.example.data.model.MeetingStatus
import com.example.data.model.Participant
import com.example.ui.components.ActionItemCard
import com.example.ui.components.DecisionCard
import com.example.ui.components.HealthScoreIndicator
import com.example.ui.components.MeetingActionItemsList
import com.example.ui.components.MeetingInviteDialog
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseError
import com.example.ui.theme.VioletAccent
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MeetingViewModel

@Composable
fun MeetingDetailScreen(
    viewModel: MeetingViewModel
) {
    val meeting by viewModel.selectedMeeting.collectAsState()
    val participants by viewModel.selectedMeetingParticipants.collectAsState()
    val agenda by viewModel.selectedMeetingAgenda.collectAsState()
    val notes by viewModel.selectedMeetingNotes.collectAsState()
    val transcript by viewModel.selectedMeetingTranscript.collectAsState()
    val summary by viewModel.selectedMeetingSummary.collectAsState()
    val actionItems by viewModel.allActionItems.collectAsState()
    val decisions by viewModel.allDecisions.collectAsState()
    val isAiGenerating by viewModel.isAiGenerating.collectAsState()
    val healthBreakdown by viewModel.healthScoreBreakdown.collectAsState()
    val context = LocalContext.current

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showInviteDialog by remember { mutableStateOf(false) }
    var showShareDialog by remember { mutableStateOf(false) }
    var newNoteText by remember { mutableStateOf("") }
    var isPrivateNote by remember { mutableStateOf(false) }

    if (meeting == null) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("No meeting selected.", style = MaterialTheme.typography.bodyLarge)
        }
        return
    }

    val currentMeeting = meeting!!
    val meetingActionItems = actionItems.filter { it.meetingId == currentMeeting.id }
    val meetingDecisions = decisions.filter { it.meetingId == currentMeeting.id }

    if (showShareDialog) {
        AlertDialog(
            onDismissRequest = { showShareDialog = false },
            title = { Text("Share Meeting Summary") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Choose how you'd like to export and share \"${currentMeeting.title}\" with participants or team channels:",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        showShareDialog = false
                        viewModel.shareMeetingSummary(context, currentMeeting, asPdf = true)
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share PDF")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        showShareDialog = false
                        viewModel.shareMeetingSummary(context, currentMeeting, asPdf = false)
                    }
                ) {
                    Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Share Text")
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("meeting_detail_screen_root"),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Meeting Header Card
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(IndigoPrimary.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = currentMeeting.category.uppercase(),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = IndigoLight
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            IconButton(
                                onClick = { showShareDialog = true },
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.surface)
                                    .testTag("meeting_detail_share_button")
                            ) {
                                Icon(Icons.Default.Share, contentDescription = "Share", modifier = Modifier.size(16.dp))
                            }

                            if (currentMeeting.status == MeetingStatus.UPCOMING) {
                                Button(
                                    onClick = { viewModel.startLiveMeeting(currentMeeting.id) },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                                    modifier = Modifier.testTag("meeting_detail_join_button")
                                ) {
                                    Icon(Icons.Default.Videocam, contentDescription = null, modifier = Modifier.size(14.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Join Room", fontSize = 11.sp)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = currentMeeting.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (currentMeeting.description.isNotBlank()) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = currentMeeting.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    if (currentMeeting.tags.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            currentMeeting.tags.forEach { tag ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(IndigoPrimary.copy(alpha = 0.12f))
                                        .padding(horizontal = 8.dp, vertical = 2.dp)
                                ) {
                                    Text("#$tag", fontSize = 11.sp, fontWeight = FontWeight.Medium, color = IndigoLight)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Date, Time, Location Strip
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "📅 ${currentMeeting.date} • ${currentMeeting.startTime} – ${currentMeeting.endTime}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "📍 ${currentMeeting.location}",
                            style = MaterialTheme.typography.bodySmall,
                            color = IndigoLight,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }

        // Detail Navigation Tabs
        item {
            val tabs = listOf("Overview & Health", "AI Summary", "Transcript", "Actions (${meetingActionItems.size})", "Agenda & Notes")
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.clip(RoundedCornerShape(10.dp))
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 11.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal
                            )
                        },
                        modifier = Modifier.testTag("meeting_tab_$index")
                    )
                }
            }
        }

        // TAB 0: OVERVIEW & HEALTH
        if (selectedTabIndex == 0) {
            item {
                HealthScoreIndicator(breakdown = healthBreakdown)
            }

            // Participants List with Speaking Distribution
            item {
                Text(
                    text = "Participants & Speaking Distribution (${participants.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(participants) { p ->
                ParticipantRow(participant = p)
            }

            // Preparation Dossier Shortcut
            item {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { viewModel.navigateTo(AppScreen.MEETING_PREPARATION, currentMeeting.id) }
                        .testTag("view_preparation_dossier_shortcut"),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CyanAccent,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = "AI Meeting Preparation Dossier",
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Strategic questions, discussion anchors & expected outcomes",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        Text("Open >", color = IndigoLight, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // TAB 1: AI SUMMARY
        if (selectedTabIndex == 1) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Automated AI Intelligence",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Generated by Gemini Flash 3.5",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyanAccent
                        )
                    }

                    Button(
                        onClick = { viewModel.generateAiSummaryForMeeting(currentMeeting) },
                        enabled = !isAiGenerating,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        modifier = Modifier.testTag("regenerate_summary_button")
                    ) {
                        if (isAiGenerating) {
                            CircularProgressIndicator(modifier = Modifier.size(14.dp), color = Color.White, strokeWidth = 2.dp)
                        } else {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(if (summary != null) "Regenerate" else "Generate Summary", fontSize = 11.sp)
                    }
                }
            }

            if (summary != null) {
                val s = summary!!
                item {
                    SummarySectionCard(title = "🎯 Meeting Objective", content = s.objective)
                }
                item {
                    SummarySectionCard(title = "📝 Key Discussion Points", content = s.keyDiscussionPoints)
                }
                item {
                    SummarySectionCard(title = "⚖️ Decisions Formulated", content = s.decisionsSummary)
                }
                item {
                    SummarySectionCard(title = "🚀 Action Items Captured", content = s.actionItemsSummary)
                }
                item {
                    SummarySectionCard(title = "📊 Sentiment & Pacing", content = "${s.sentimentScore}\nRisks: ${s.riskFactors}")
                }
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                viewModel.shareMeetingSummary(context, currentMeeting, asPdf = false)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_summary_text_button"),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(Icons.Default.TextFields, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share Text", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                viewModel.shareMeetingSummary(context, currentMeeting, asPdf = true)
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("share_summary_pdf_button"),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                        ) {
                            Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Share PDF", fontSize = 12.sp)
                        }
                    }
                }
            } else {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(160.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("No summary generated yet for this session.")
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Tap 'Generate Summary' to analyze transcript and extract key outcomes.", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                }
            }
        }

        // TAB 2: TRANSCRIPT
        if (selectedTabIndex == 2) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Real-Time Meeting Transcript (${transcript.size} lines)",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Multi-speaker diarization",
                        style = MaterialTheme.typography.labelSmall,
                        color = IndigoLight
                    )
                }
            }

            if (transcript.isEmpty()) {
                item {
                    Text(
                        text = "No live transcript captured yet. Join the live room to capture real-time speech.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                items(transcript) { line ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = line.speaker,
                                        fontWeight = FontWeight.Bold,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = IndigoLight
                                    )
                                    if (line.isDecision) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(VioletAccent.copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("DECISION", fontSize = 9.sp, color = VioletAccent, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    if (line.isActionItem) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(EmeraldSuccess.copy(alpha = 0.2f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("ACTION", fontSize = 9.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text(
                                    text = line.timeLabel,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = line.text,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // TAB 3: ACTION ITEMS & DECISIONS
        if (selectedTabIndex == 3) {
            item {
                MeetingActionItemsList(
                    meetingId = currentMeeting.id,
                    actionItems = meetingActionItems,
                    onToggleStatus = { itemId, newStatus ->
                        viewModel.updateActionItemStatus(itemId, newStatus)
                    },
                    onAddActionItem = { title, assignee, dueDate, priority ->
                        viewModel.addActionItemForMeeting(
                            meetingId = currentMeeting.id,
                            meetingTitle = currentMeeting.title,
                            title = title,
                            assigneeName = assignee,
                            dueDate = dueDate,
                            priority = priority
                        )
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "Meeting Decisions (${meetingDecisions.size})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            items(meetingDecisions) { decision ->
                DecisionCard(decision = decision)
            }
        }

        // TAB 4: AGENDA & NOTES
        if (selectedTabIndex == 4) {
            item {
                Text(
                    text = "Agenda Pacing & Checklist (${agenda.size} topics)",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            items(agenda) { item ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { viewModel.toggleAgendaItem(item) },
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (item.isCompleted) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                            contentDescription = null,
                            tint = if (item.isCompleted) EmeraldSuccess else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${item.orderIndex}. ${item.title}",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Allocated: ${item.durationMinutes}m • Presenter: ${item.presenter}",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            // Notes Section
            item {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Collaborative & Private Notes",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }

            items(notes) { note ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp)),
                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                ) {
                    Column(modifier = Modifier.padding(10.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = note.authorName,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.bodySmall,
                                color = IndigoLight
                            )
                            if (note.isPrivate) {
                                Text("🔒 Private Note", fontSize = 10.sp, color = AmberWarning)
                            }
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = note.text,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            // Add Note Input Bar
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = newNoteText,
                        onValueChange = { newNoteText = it },
                        placeholder = { Text("Add meeting note...", fontSize = 12.sp) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Button(
                        onClick = {
                            if (newNoteText.isNotBlank()) {
                                viewModel.addLiveNote(currentMeeting.id, newNoteText, isPrivateNote)
                                newNoteText = ""
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                    ) {
                        Text("Add")
                    }
                }
            }
        }
    }

    MeetingInviteDialog(
        isOpen = showInviteDialog,
        meeting = currentMeeting,
        onDismiss = { showInviteDialog = false }
    )
}

@Composable
private fun ParticipantRow(participant: Participant) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(IndigoPrimary),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = participant.name.take(2).uppercase(),
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 11.sp
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = participant.name,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (participant.isOrganizer) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(IndigoPrimary.copy(alpha = 0.2f))
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("HOST", fontSize = 9.sp, color = IndigoLight, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                    Text(
                        text = "${participant.role} • ${participant.email}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (participant.speakingPercentage > 0) {
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${participant.speakingPercentage}% spoke",
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = CyanAccent
                    )
                    Text(
                        text = "${participant.speakingTimeSeconds / 60}m ${participant.speakingTimeSeconds % 60}s",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SummarySectionCard(title: String, content: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = IndigoLight
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = content,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
