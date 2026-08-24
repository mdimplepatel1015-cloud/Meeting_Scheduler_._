package com.example.ui.screens

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
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.ui.components.AgendaGeneratorDialog
import com.example.ui.components.SmartScheduleDialog
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
fun MeetingCreateScreen(
    viewModel: MeetingViewModel
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Engineering") }
    var meetingType by remember { mutableStateOf(MeetingType.TEAM_MEETING) }
    var date by remember { mutableStateOf("2026-08-25") }
    var startTime by remember { mutableStateOf("11:00 AM") }
    var endTime by remember { mutableStateOf("11:45 AM") }
    var durationMinutes by remember { mutableIntStateOf(45) }
    var location by remember { mutableStateOf("Google Meet") }
    var meetingLink by remember { mutableStateOf("https://meet.google.com/new-meeting-iq") }
    var priority by remember { mutableStateOf(MeetingPriority.HIGH) }
    var reminderMinutesBefore by remember { mutableIntStateOf(15) }
    var recurrence by remember { mutableStateOf(MeetingRecurrence.NONE) }
    var visibility by remember { mutableStateOf(MeetingVisibility.PRIVATE) }

    val attendeeEmails = remember { mutableStateListOf("rahul.s@enterprise.ai", "priya.p@enterprise.ai") }
    var newAttendeeInput by remember { mutableStateOf("") }

    val agendaItems = remember {
        mutableStateListOf(
            "Project Objectives & Key Goals (10 min)",
            "Architecture Design Review & Technical Discussion (20 min)",
            "Next Steps, Assigned Owners & Deadlines (15 min)"
        )
    }
    var newAgendaInput by remember { mutableStateOf("") }

    var showSmartScheduleDialog by remember { mutableStateOf(false) }
    var showAgendaGeneratorDialog by remember { mutableStateOf(false) }

    val isAiGenerating by viewModel.isAiGenerating.collectAsState()
    val smartSlots by viewModel.smartScheduleSlots.collectAsState()
    val generatedAgenda by viewModel.generatedAgendaList.collectAsState()

    val durationPresets = listOf(15, 30, 45, 60, 90)
    val categories = listOf("Engineering", "Product", "People", "DevOps", "Sales", "Executive")
    val locations = listOf("Google Meet", "Zoom", "Microsoft Teams", "Slack Huddle", "In-Person (Room 4A)")

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("meeting_create_screen_root"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        item {
            Text(
                text = "Schedule New Meeting",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Configure parameters with AI scheduling assistance and agenda optimization.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Title & Description Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Meeting Title *") },
                        placeholder = { Text("e.g. Q4 Architecture Planning") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_meeting_title_input"),
                        shape = RoundedCornerShape(10.dp),
                        singleLine = true
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = description,
                        onValueChange = { description = it },
                        label = { Text("Description / Objectives") },
                        placeholder = { Text("Outline meeting goals and topics...") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("create_meeting_description_input"),
                        shape = RoundedCornerShape(10.dp),
                        maxLines = 3
                    )
                }
            }
        }

        // Category & Type Selectors
        item {
            Column {
                Text(
                    text = "Category",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categories) { cat ->
                        val isSelected = category == cat
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { category = cat }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = cat,
                                fontSize = 12.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Date, Duration & AI Smart Scheduling Finder
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Time & Availability",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = {
                                viewModel.requestSmartSchedule(
                                    title.ifBlank { "Team Meeting" },
                                    attendeeEmails.size + 1,
                                    durationMinutes
                                )
                                showSmartScheduleDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyanAccent.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("open_smart_schedule_button")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CyanAccent, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Find Best Time (AI)", fontSize = 11.sp, color = CyanAccent, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedTextField(
                            value = date,
                            onValueChange = { date = it },
                            label = { Text("Date (YYYY-MM-DD)") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                        OutlinedTextField(
                            value = startTime,
                            onValueChange = { startTime = it },
                            label = { Text("Start Time") },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text("Duration Preset", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        durationPresets.forEach { preset ->
                            val isSelected = durationMinutes == preset
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) IndigoLight else MaterialTheme.colorScheme.surface)
                                    .clickable {
                                        durationMinutes = preset
                                        // Calculate end time
                                        endTime = if (preset == 30) "11:30 AM" else if (preset == 45) "11:45 AM" else "12:00 PM"
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = "${preset}m",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
        }

        // Platform & Link
        item {
            Column {
                Text(
                    text = "Meeting Platform / Location",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(locations) { loc ->
                        val isSelected = location == loc
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { location = loc }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = loc,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // AI Agenda Generator Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Meeting Agenda (${agendaItems.size} items)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold
                        )

                        Button(
                            onClick = {
                                viewModel.generateAiAgenda(title.ifBlank { "Team Strategy Session" }, meetingType, durationMinutes)
                                showAgendaGeneratorDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary.copy(alpha = 0.2f)),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("open_ai_agenda_architect_button")
                        ) {
                            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = IndigoLight, modifier = Modifier.size(14.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Draft with AI", fontSize = 11.sp, color = IndigoLight, fontWeight = FontWeight.Bold)
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    agendaItems.forEachIndexed { index, item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "${index + 1}. $item",
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = { agendaItems.removeAt(index) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(Icons.Default.Close, contentDescription = "Remove", modifier = Modifier.size(14.dp))
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newAgendaInput,
                            onValueChange = { newAgendaInput = it },
                            placeholder = { Text("Add custom agenda topic...", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (newAgendaInput.isNotBlank()) {
                                    agendaItems.add(newAgendaInput)
                                    newAgendaInput = ""
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

        // Attendees Invitation Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Participants & Attendees",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Existing Attendees Chips
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                        items(attendeeEmails) { email ->
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = IndigoPrimary.copy(alpha = 0.15f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(text = email, fontSize = 11.sp, color = IndigoLight)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        modifier = Modifier
                                            .size(12.dp)
                                            .clickable { attendeeEmails.remove(email) },
                                        tint = IndigoLight
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = newAttendeeInput,
                            onValueChange = { newAttendeeInput = it },
                            placeholder = { Text("Enter email (e.g. sarah.j@enterprise.ai)...", fontSize = 11.sp) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            singleLine = true
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Button(
                            onClick = {
                                if (newAttendeeInput.isNotBlank()) {
                                    attendeeEmails.add(newAttendeeInput)
                                    newAttendeeInput = ""
                                }
                            },
                            shape = RoundedCornerShape(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
                        ) {
                            Text("Invite")
                        }
                    }
                }
            }
        }

        // Recurrence & Priority Row
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text("Priority", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(MeetingPriority.HIGH, MeetingPriority.MEDIUM, MeetingPriority.LOW).forEach { p ->
                            val isSelected = priority == p
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isSelected) {
                                            when (p) {
                                                MeetingPriority.HIGH -> RoseError
                                                MeetingPriority.MEDIUM -> AmberWarning
                                                MeetingPriority.LOW -> EmeraldSuccess
                                            }
                                        } else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .clickable { priority = p }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = p.name.take(3),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }

                Column(modifier = Modifier.weight(1f)) {
                    Text("Recurrence", style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        listOf(MeetingRecurrence.NONE to "Once", MeetingRecurrence.WEEKLY to "Weekly", MeetingRecurrence.BIWEEKLY to "Bi-Wk").forEach { (rec, label) ->
                            val isSelected = recurrence == rec
                            Box(
                                modifier = Modifier
                                    .weight(1f)
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(if (isSelected) IndigoLight else MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { recurrence = rec }
                                    .padding(vertical = 6.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }

        // Action Buttons: Schedule & Cancel
        item {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        viewModel.createMeeting(
                            title = title,
                            description = description,
                            type = meetingType,
                            category = category,
                            date = date,
                            startTime = startTime,
                            endTime = endTime,
                            durationMinutes = durationMinutes,
                            timezone = "PST (UTC-7)",
                            location = location,
                            meetingLink = meetingLink,
                            priority = priority,
                            reminderMinutesBefore = reminderMinutesBefore,
                            recurrence = recurrence,
                            visibility = visibility,
                            attendeeEmails = attendeeEmails,
                            agendaTitles = agendaItems,
                            onSuccess = { meetingId ->
                                viewModel.navigateTo(AppScreen.MEETING_DETAIL, meetingId)
                            }
                        )
                    }
                },
                enabled = title.isNotBlank(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag("submit_schedule_meeting_button"),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary)
            ) {
                Icon(Icons.Default.CalendarToday, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Confirm & Schedule Meeting", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            }
        }
    }

    SmartScheduleDialog(
        isOpen = showSmartScheduleDialog,
        isLoading = isAiGenerating,
        slots = smartSlots,
        onSelectSlot = { slot ->
            date = "2026-08-25"
            startTime = slot.timeRange.substringBefore(" – ")
            endTime = slot.timeRange.substringAfter(" – ")
        },
        onDismiss = { showSmartScheduleDialog = false }
    )

    AgendaGeneratorDialog(
        isOpen = showAgendaGeneratorDialog,
        isLoading = isAiGenerating,
        generatedAgenda = generatedAgenda,
        onApplyAgenda = { items ->
            agendaItems.clear()
            agendaItems.addAll(items)
        },
        onDismiss = { showAgendaGeneratorDialog = false }
    )
}
