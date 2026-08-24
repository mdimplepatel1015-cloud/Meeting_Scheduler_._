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
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Chat
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FiberManualRecord
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.GraphicEq
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.model.MeetingStatus
import com.example.data.model.Participant
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseError
import com.example.ui.theme.Slate950
import com.example.ui.theme.VioletAccent
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MeetingViewModel
import java.util.Locale

@Composable
fun LiveMeetingScreen(
    viewModel: MeetingViewModel
) {
    val meeting by viewModel.selectedMeeting.collectAsState()
    val participants by viewModel.selectedMeetingParticipants.collectAsState()
    val transcript by viewModel.selectedMeetingTranscript.collectAsState()
    val chatMessages by viewModel.selectedMeetingChat.collectAsState()
    val liveSeconds by viewModel.liveMeetingSeconds.collectAsState()
    val isMicMuted by viewModel.isLiveMicMuted.collectAsState()
    val isVideoEnabled by viewModel.isLiveVideoEnabled.collectAsState()
    val isRecording by viewModel.isLiveRecording.collectAsState()
    val isAiGenerating by viewModel.isAiGenerating.collectAsState()

    val isSpeechListening by viewModel.isSpeechListening.collectAsState()
    val speechRecognizedText by viewModel.speechRecognizedText.collectAsState()
    val speechPartialText by viewModel.speechPartialText.collectAsState()
    val speechError by viewModel.speechError.collectAsState()

    val context = LocalContext.current
    var selectedTab by remember { mutableIntStateOf(0) }
    var speechInputText by remember { mutableStateOf("") }
    var chatInputText by remember { mutableStateOf("") }
    var selectedSpeaker by remember { mutableStateOf("Rahul Sharma") }
    var isTaggedAsDecision by remember { mutableStateOf(false) }
    var isTaggedAsAction by remember { mutableStateOf(false) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            viewModel.startLiveSpeechRecognition { recognized ->
                speechInputText = recognized
            }
        } else {
            Toast.makeText(context, "Microphone permission is required for live speech transcription", Toast.LENGTH_SHORT).show()
        }
    }

    LaunchedEffect(speechRecognizedText) {
        if (speechRecognizedText.isNotBlank()) {
            speechInputText = speechRecognizedText
        }
    }

    val transcriptListState = rememberLazyListState()

    LaunchedEffect(transcript.size) {
        if (transcript.isNotEmpty()) {
            transcriptListState.animateScrollToItem(transcript.size - 1)
        }
    }

    val mins = liveSeconds / 60
    val secs = liveSeconds % 60
    val formattedTime = String.format(Locale.US, "%02d:%02d", mins, secs)

    // Flashing Recording Indicator animation
    val infiniteTransition = rememberInfiniteTransition(label = "recordingPulse")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Slate950)
            .padding(16.dp)
            .testTag("live_meeting_screen_root")
    ) {
        // Live Room Header & Timer
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.FiberManualRecord,
                        contentDescription = "Recording",
                        tint = RoseError.copy(alpha = if (isRecording) alphaAnim else 0.4f),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "REC • $formattedTime",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }
                Text(
                    text = meeting?.title ?: "Live Meeting Session",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            // End Call Button
            Button(
                onClick = {
                    viewModel.stopLiveMeeting()
                    if (meeting != null) {
                        viewModel.generateAiSummaryForMeeting(meeting!!)
                    }
                    viewModel.navigateTo(AppScreen.MEETING_DETAIL)
                },
                colors = ButtonDefaults.buttonColors(containerColor = RoseError),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier.testTag("end_live_meeting_button")
            ) {
                Icon(Icons.Default.CallEnd, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("End & Summarize", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Visual Progress Bar: Scheduled End Time & Time Remaining
        val scheduledMinutes = meeting?.durationMinutes ?: 45
        val totalDurationSeconds = (scheduledMinutes * 60).coerceAtLeast(60)
        val remainingSeconds = totalDurationSeconds - liveSeconds
        val isOvertime = remainingSeconds < 0
        val progressFraction = (liveSeconds.toFloat() / totalDurationSeconds.toFloat()).coerceIn(0f, 1f)

        val (barColor, barBgColor, statusLabel) = when {
            isOvertime -> Triple(RoseError, RoseError.copy(alpha = 0.2f), "OVERTIME")
            remainingSeconds <= 300 -> Triple(RoseError, RoseError.copy(alpha = 0.2f), "ENDING SOON")
            remainingSeconds <= 600 -> Triple(AmberWarning, AmberWarning.copy(alpha = 0.2f), "WRAP UP")
            else -> Triple(EmeraldSuccess, EmeraldSuccess.copy(alpha = 0.2f), "ON TRACK")
        }

        val remainingFormatted = if (isOvertime) {
            val overtimeSecs = -remainingSeconds
            String.format(Locale.US, "+%02d:%02d Overtime", overtimeSecs / 60, overtimeSecs % 60)
        } else {
            String.format(Locale.US, "%02d:%02d remaining", remainingSeconds / 60, remainingSeconds % 60)
        }

        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .border(1.dp, barColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
                .testTag("live_meeting_progress_bar_container"),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = if (isOvertime) Icons.Default.HourglassBottom else Icons.Default.AccessTime,
                            contentDescription = null,
                            tint = barColor,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Scheduled: ${scheduledMinutes}m",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "•  $remainingFormatted",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = barColor
                        )
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(barBgColor)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = statusLabel,
                                fontSize = 9.sp,
                                fontWeight = FontWeight.Bold,
                                color = barColor
                            )
                        }

                        // Time warning notification trigger
                        IconButton(
                            onClick = {
                                val currentMeeting = meeting
                                if (currentMeeting != null) {
                                    viewModel.triggerManualTimeNearlyUpNotification(currentMeeting, remainingSeconds.toLong().coerceAtLeast(60L))
                                    Toast.makeText(context, "Time warning alert notification sent!", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier
                                .size(24.dp)
                                .testTag("notify_time_up_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Send Time Nearly Up Alert",
                                tint = barColor,
                                modifier = Modifier.size(15.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                LinearProgressIndicator(
                    progress = { progressFraction },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
                        .testTag("live_meeting_progress_indicator"),
                    color = barColor,
                    trackColor = Color.White.copy(alpha = 0.1f),
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Participants Video/Avatar Grid Preview
        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(participants) { p ->
                Surface(
                    modifier = Modifier
                        .width(130.dp)
                        .height(90.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .border(
                            1.dp,
                            if (p.name == selectedSpeaker) CyanAccent else Color.White.copy(alpha = 0.1f),
                            RoundedCornerShape(12.dp)
                        )
                        .clickable { selectedSpeaker = p.name },
                    color = MaterialTheme.colorScheme.surfaceVariant
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        Column(
                            modifier = Modifier
                                .align(Alignment.Center)
                                .padding(6.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(IndigoPrimary),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = p.name.take(2).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = p.name.substringBefore(" "),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        if (p.name == selectedSpeaker) {
                            Box(
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(6.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyanAccent)
                                    .padding(horizontal = 4.dp, vertical = 2.dp)
                            ) {
                                Text("SPEAKING", fontSize = 8.sp, fontWeight = FontWeight.Bold, color = Slate950)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Tab Selector for Live Transcript vs Chat
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
            contentColor = IndigoLight,
            modifier = Modifier.clip(RoundedCornerShape(8.dp))
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Live AI Transcript (${transcript.size})", fontSize = 12.sp) }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Meeting Chat & AI Bot", fontSize = 12.sp) }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // TAB 0: LIVE TRANSCRIPT STREAM
        if (selectedTab == 0) {
            LazyColumn(
                state = transcriptListState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(transcript) { line ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp)),
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
                                        fontSize = 12.sp,
                                        color = CyanAccent
                                    )
                                    if (line.isDecision) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(VioletAccent.copy(alpha = 0.3f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("DECISION", fontSize = 8.sp, color = VioletAccent, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                    if (line.isActionItem) {
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(EmeraldSuccess.copy(alpha = 0.3f))
                                                .padding(horizontal = 4.dp, vertical = 2.dp)
                                        ) {
                                            Text("ACTION ITEM", fontSize = 8.sp, color = EmeraldSuccess, fontWeight = FontWeight.Bold)
                                        }
                                    }
                                }
                                Text(text = line.timeLabel, fontSize = 10.sp, color = Color.White.copy(alpha = 0.6f))
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(text = line.text, fontSize = 13.sp, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Android SpeechRecognizer API & Real-time Transcript Input Bar
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    // Header with Speaker Selection & Voice Listening status
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Speaker: $selectedSpeaker",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CyanAccent
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp), verticalAlignment = Alignment.CenterVertically) {
                            // Tag as Decision
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isTaggedAsDecision) VioletAccent else Color.Transparent)
                                    .clickable { isTaggedAsDecision = !isTaggedAsDecision }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("⚖️ Decision", fontSize = 10.sp, color = if (isTaggedAsDecision) Color.White else Color.White.copy(alpha = 0.7f))
                            }

                            // Tag as Action
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(if (isTaggedAsAction) EmeraldSuccess else Color.Transparent)
                                    .clickable { isTaggedAsAction = !isTaggedAsAction }
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text("🚀 Action", fontSize = 10.sp, color = if (isTaggedAsAction) Color.White else Color.White.copy(alpha = 0.7f))
                            }
                        }
                    }

                    // Live Partial Speech recognition stream preview
                    if (isSpeechListening || speechPartialText.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(6.dp))
                                .background(IndigoPrimary.copy(alpha = 0.15f))
                                .padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = EmeraldSuccess,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (speechPartialText.isNotBlank()) "Listening: \"$speechPartialText\"" else "Listening for speech...",
                                fontSize = 11.sp,
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    if (speechError != null) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = speechError ?: "",
                            fontSize = 10.sp,
                            color = RoseError
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Real-Time Transcript Input Field with Speech Recognizer Mic Button
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = speechInputText,
                            onValueChange = { speechInputText = it },
                            placeholder = { Text("Real-time live transcript input...", fontSize = 12.sp) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("live_speech_input"),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = false,
                            maxLines = 3,
                            trailingIcon = {
                                IconButton(
                                    onClick = {
                                        if (isSpeechListening) {
                                            viewModel.stopLiveSpeechRecognition()
                                        } else {
                                            val hasPermission = ContextCompat.checkSelfPermission(
                                                context,
                                                Manifest.permission.RECORD_AUDIO
                                            ) == PackageManager.PERMISSION_GRANTED

                                            if (hasPermission) {
                                                viewModel.startLiveSpeechRecognition { text ->
                                                    speechInputText = text
                                                }
                                            } else {
                                                micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                                            }
                                        }
                                    },
                                    modifier = Modifier.testTag("speech_recognizer_mic_btn")
                                ) {
                                    Icon(
                                        imageVector = if (isSpeechListening) Icons.Default.Stop else Icons.Default.Mic,
                                        contentDescription = "Voice Input",
                                        tint = if (isSpeechListening) RoseError else IndigoLight,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        Button(
                            onClick = {
                                if (speechInputText.isNotBlank() && meeting != null) {
                                    viewModel.addLiveTranscriptLine(
                                        meeting!!.id,
                                        selectedSpeaker,
                                        speechInputText,
                                        isTaggedAsDecision,
                                        isTaggedAsAction
                                    )
                                    speechInputText = ""
                                    viewModel.clearLiveSpeechState()
                                    isTaggedAsDecision = false
                                    isTaggedAsAction = false
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                            modifier = Modifier.testTag("send_live_speech_button")
                        ) {
                            Text("Post", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Real-time AI Summary Trigger Button
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${transcript.size} utterances captured",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )

                        Button(
                            onClick = {
                                if (meeting != null) {
                                    viewModel.generateMeetingSummary(meeting!!.id)
                                }
                            },
                            enabled = !isAiGenerating && transcript.isNotEmpty(),
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = VioletAccent),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier.testTag("generate_summary_live_button")
                        ) {
                            if (isAiGenerating) {
                                CircularProgressIndicator(modifier = Modifier.size(12.dp), color = Color.White, strokeWidth = 2.dp)
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("Summarizing...", fontSize = 11.sp)
                            } else {
                                Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Process Summary (Gemini AI)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // TAB 1: LIVE CHAT
        if (selectedTab == 1) {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(chatMessages) { msg ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp)),
                        color = if (msg.isAiAssistant) IndigoPrimary.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
                    ) {
                        Column(modifier = Modifier.padding(8.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = if (msg.isAiAssistant) "🤖 ${msg.senderName}" else msg.senderName,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 11.sp,
                                    color = if (msg.isAiAssistant) IndigoLight else CyanAccent
                                )
                                Text(text = msg.timestamp, fontSize = 9.sp, color = Color.White.copy(alpha = 0.5f))
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(text = msg.text, fontSize = 12.sp, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = chatInputText,
                    onValueChange = { chatInputText = it },
                    placeholder = { Text("Send in-meeting message...", fontSize = 12.sp) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(8.dp),
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = {
                        if (chatInputText.isNotBlank() && meeting != null) {
                            viewModel.sendLiveChatMessage(meeting!!.id, chatInputText)
                            chatInputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(IndigoPrimary)
                ) {
                    Icon(Icons.AutoMirrored.Filled.Send, contentDescription = "Send", tint = Color.White, modifier = Modifier.size(18.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Hardware Controls Bar (Mic, Cam, Audio)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { viewModel.isLiveMicMuted.value = !isMicMuted },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isMicMuted) RoseError else MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = if (isMicMuted) Icons.Default.MicOff else Icons.Default.Mic,
                    contentDescription = "Mute Mic",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = { viewModel.isLiveVideoEnabled.value = !isVideoEnabled },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (!isVideoEnabled) RoseError else MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = if (isVideoEnabled) Icons.Default.Videocam else Icons.Default.VideocamOff,
                    contentDescription = "Toggle Video",
                    tint = Color.White
                )
            }

            IconButton(
                onClick = { viewModel.isLiveRecording.value = !isRecording },
                modifier = Modifier
                    .size(48.dp)
                    .clip(CircleShape)
                    .background(if (isRecording) EmeraldSuccess.copy(alpha = 0.2f) else MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.GraphicEq,
                    contentDescription = "Toggle Audio Analysis",
                    tint = if (isRecording) EmeraldSuccess else Color.White
                )
            }
        }
    }
}
