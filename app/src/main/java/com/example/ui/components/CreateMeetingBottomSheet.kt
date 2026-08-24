package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.VideoCall
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.MeetingPriority
import com.example.data.model.MeetingRecurrence
import com.example.data.model.MeetingType
import com.example.data.model.MeetingVisibility
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.VioletAccent
import com.example.ui.viewmodel.MeetingViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun CreateMeetingBottomSheet(
    isOpen: Boolean,
    onDismiss: () -> Unit,
    viewModel: MeetingViewModel,
    initialDate: String = "2026-08-25"
) {
    if (!isOpen) return

    val sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var date by remember { mutableStateOf(initialDate) }
    var startTime by remember { mutableStateOf("10:00 AM") }
    var endTime by remember { mutableStateOf("10:45 AM") }
    var category by remember { mutableStateOf("Engineering") }
    var meetingType by remember { mutableStateOf(MeetingType.TEAM_MEETING) }
    var newParticipantEmail by remember { mutableStateOf("") }
    val participants = remember {
        mutableStateListOf(
            "rahul.sharma@enterprise.ai",
            "priya.patel@enterprise.ai"
        )
    }

    val categories = listOf("Engineering", "Product", "Design", "DevOps", "Sales", "Executive")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.testTag("create_meeting_bottom_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = null,
                            tint = IndigoLight,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "New Meeting",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Schedule session and notify attendees",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_create_meeting_sheet")
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Meeting Title
            Text(
                text = "Meeting Title *",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("e.g. Q3 Architecture & Security Sync") },
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("input_meeting_title"),
                shape = RoundedCornerShape(10.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = IndigoPrimary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant
                ),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Date & Time inputs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Date *",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = date,
                        onValueChange = { date = it },
                        modifier = Modifier.fillMaxWidth().testTag("input_meeting_date"),
                        shape = RoundedCornerShape(8.dp),
                        leadingIcon = {
                            Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        singleLine = true
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Start Time *",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = startTime,
                        onValueChange = { startTime = it },
                        modifier = Modifier.fillMaxWidth().testTag("input_meeting_start_time"),
                        shape = RoundedCornerShape(8.dp),
                        leadingIcon = {
                            Icon(Icons.Default.Schedule, contentDescription = null, modifier = Modifier.size(16.dp))
                        },
                        singleLine = true
                    )
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "End Time",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedTextField(
                        value = endTime,
                        onValueChange = { endTime = it },
                        modifier = Modifier.fillMaxWidth().testTag("input_meeting_end_time"),
                        shape = RoundedCornerShape(8.dp),
                        singleLine = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Category Chips
            Text(
                text = "Category",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                categories.forEach { cat ->
                    val isSelected = category == cat
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier.clickable { category = cat }
                    ) {
                        Text(
                            text = cat,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Participants Input
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Participants (${participants.size})",
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.sp
                )
                Text(
                    text = "Auto-RSVP invite sent",
                    fontSize = 11.sp,
                    color = EmeraldSuccess
                )
            }
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = newParticipantEmail,
                    onValueChange = { newParticipantEmail = it },
                    placeholder = { Text("colleague@enterprise.ai", fontSize = 12.sp) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("input_participant_email"),
                    shape = RoundedCornerShape(8.dp),
                    leadingIcon = {
                        Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    },
                    singleLine = true
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        if (newParticipantEmail.isNotBlank() && !participants.contains(newParticipantEmail.trim())) {
                            participants.add(newParticipantEmail.trim())
                            newParticipantEmail = ""
                        }
                    },
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                    modifier = Modifier.testTag("add_participant_btn")
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add")
                }
            }

            if (participants.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    participants.forEach { email ->
                        Surface(
                            shape = RoundedCornerShape(16.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            modifier = Modifier.border(0.5.dp, IndigoPrimary.copy(alpha = 0.3f), RoundedCornerShape(16.dp))
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = email.substringBefore("@"),
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Remove",
                                    modifier = Modifier
                                        .size(12.dp)
                                        .clickable { participants.remove(email) },
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Description
            Text(
                text = "Meeting Description",
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                placeholder = { Text("Brief context and objectives for participants...", fontSize = 12.sp) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .testTag("input_meeting_description"),
                shape = RoundedCornerShape(10.dp),
                maxLines = 3
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Cancel")
                }

                Button(
                    onClick = {
                        val finalTitle = if (title.isBlank()) "Quick Sync (${category})" else title.trim()
                        val finalDescription = if (description.isBlank()) "Scheduled via MeetIQ Quick Sheet" else description.trim()
                        val defaultAgendas = listOf(
                            "Welcome & Review Objectives (5 min)",
                            "Core Discussion & Technical Alignment (25 min)",
                            "Action Items & Wrap-up (10 min)"
                        )

                        viewModel.createMeeting(
                            title = finalTitle,
                            description = finalDescription,
                            type = meetingType,
                            category = category,
                            date = date,
                            startTime = startTime,
                            endTime = endTime,
                            durationMinutes = 45,
                            timezone = "PST (UTC-7)",
                            location = "Google Meet",
                            meetingLink = "https://meet.google.com/iq-${(1000..9999).random()}",
                            priority = MeetingPriority.HIGH,
                            reminderMinutesBefore = 15,
                            recurrence = MeetingRecurrence.NONE,
                            visibility = MeetingVisibility.PRIVATE,
                            attendeeEmails = participants.toList(),
                            agendaTitles = defaultAgendas,
                            onSuccess = {
                                onDismiss()
                            }
                        )
                    },
                    modifier = Modifier
                        .weight(1.5f)
                        .testTag("save_new_meeting_btn"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Schedule Meeting", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
