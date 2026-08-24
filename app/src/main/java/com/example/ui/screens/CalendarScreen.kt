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
import androidx.compose.material.icons.automirrored.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Meeting
import com.example.data.model.MeetingPriority
import com.example.data.model.MeetingStatus
import com.example.ui.components.CreateMeetingBottomSheet
import com.example.ui.components.MeetingCard
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseError
import com.example.ui.theme.VioletAccent
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.CalendarViewMode
import com.example.ui.viewmodel.MeetingViewModel

data class CalendarDayItem(
    val dayName: String,
    val dayNumber: String,
    val fullDate: String,
    val isToday: Boolean,
    val hasEvents: Boolean
)

@Composable
fun CalendarScreen(
    viewModel: MeetingViewModel
) {
    val meetings by viewModel.allMeetings.collectAsState()
    val viewMode by viewModel.calendarViewMode.collectAsState()
    val selectedDate by viewModel.selectedCalendarDate.collectAsState()

    val daysOfWeek = listOf(
        CalendarDayItem("Sun", "23", "2026-08-23", true, true),
        CalendarDayItem("Mon", "24", "2026-08-24", false, true),
        CalendarDayItem("Tue", "25", "2026-08-25", false, true),
        CalendarDayItem("Wed", "26", "2026-08-26", false, true),
        CalendarDayItem("Thu", "27", "2026-08-27", false, false),
        CalendarDayItem("Fri", "28", "2026-08-28", false, true),
        CalendarDayItem("Sat", "29", "2026-08-29", false, false)
    )

    val selectedDayMeetings = meetings.filter { it.date == selectedDate }
    var isCreateSheetOpen by remember { mutableStateOf(false) }

    val timeSlots = listOf(
        "09:00 AM", "10:00 AM", "11:00 AM", "12:00 PM",
        "01:00 PM", "02:00 PM", "03:00 PM", "04:00 PM", "05:00 PM"
    )

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
                .testTag("calendar_screen_root"),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 88.dp)
        ) {
        // Month Navigation & View Mode Toggle
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "August 2026",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ChevronLeft, contentDescription = "Prev")
                    }
                    IconButton(onClick = {}, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.ChevronRight, contentDescription = "Next")
                    }
                }

                // View Mode Chips
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(2.dp)
                ) {
                    listOf(CalendarViewMode.DAY, CalendarViewMode.WEEK, CalendarViewMode.MONTH, CalendarViewMode.AGENDA).forEach { mode ->
                        val isSelected = viewMode == mode
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(if (isSelected) IndigoPrimary else Color.Transparent)
                                .clickable { viewModel.calendarViewMode.value = mode }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                .testTag("calendar_mode_${mode.name.lowercase()}")
                        ) {
                            Text(
                                text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                                fontSize = 11.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }

        // Days of Week Strip Selector
        item {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(daysOfWeek) { day ->
                    val isSelected = day.fullDate == selectedDate
                    Surface(
                        modifier = Modifier
                            .width(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .border(
                                1.dp,
                                if (isSelected) IndigoLight else if (day.isToday) CyanAccent.copy(alpha = 0.5f) else Color.Transparent,
                                RoundedCornerShape(12.dp)
                            )
                            .clickable { viewModel.selectedCalendarDate.value = day.fullDate }
                            .testTag("calendar_day_chip_${day.dayNumber}"),
                        color = if (isSelected) IndigoPrimary else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = day.dayName,
                                fontSize = 11.sp,
                                color = if (isSelected) Color.White.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = day.dayNumber,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            if (day.hasEvents) {
                                Box(
                                    modifier = Modifier
                                        .size(4.dp)
                                        .clip(CircleShape)
                                        .background(if (isSelected) CyanAccent else IndigoLight)
                                )
                            } else {
                                Spacer(modifier = Modifier.height(4.dp))
                            }
                        }
                    }
                }
            }
        }

        // Selected Date Summary Bar
        item {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (selectedDate == "2026-08-23") "Today's Schedule (Aug 23)" else "Schedule for $selectedDate",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${selectedDayMeetings.size} events • Working Hours: 09:00 AM – 05:30 PM",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Button(
                        onClick = { viewModel.navigateTo(AppScreen.MEETING_CREATE) },
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = IndigoPrimary),
                        modifier = Modifier.testTag("calendar_schedule_slot_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add Event", fontSize = 11.sp)
                    }
                }
            }
        }

        // Time Grid Layout
        items(timeSlots) { timeSlot ->
            val matchingMeeting = selectedDayMeetings.find { it.startTime.startsWith(timeSlot.take(2)) }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Time Label
                Text(
                    text = timeSlot,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.width(68.dp)
                )

                if (matchingMeeting != null) {
                    // Scheduled Meeting Box
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                1.dp,
                                when (matchingMeeting.priority) {
                                    MeetingPriority.HIGH -> RoseError.copy(alpha = 0.4f)
                                    MeetingPriority.MEDIUM -> CyanAccent.copy(alpha = 0.4f)
                                    MeetingPriority.LOW -> EmeraldSuccess.copy(alpha = 0.4f)
                                },
                                RoundedCornerShape(10.dp)
                            )
                            .clickable { viewModel.selectMeeting(matchingMeeting.id) }
                            .testTag("calendar_meeting_block_${matchingMeeting.id}"),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Column(modifier = Modifier.padding(10.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${matchingMeeting.startTime} – ${matchingMeeting.endTime}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = IndigoLight,
                                    fontWeight = FontWeight.Bold
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(IndigoPrimary.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = matchingMeeting.category,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IndigoLight
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = matchingMeeting.title,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )

                            Spacer(modifier = Modifier.height(2.dp))

                            Text(
                                text = "📍 ${matchingMeeting.location}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                } else {
                    // Available Free Slot
                    Surface(
                        modifier = Modifier
                            .weight(1f)
                            .height(42.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .border(
                                1.dp,
                                MaterialTheme.colorScheme.outline.copy(alpha = 0.15f),
                                RoundedCornerShape(8.dp)
                            )
                            .clickable {
                                isCreateSheetOpen = true
                            },
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.3f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = "Free Focus Slot",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Text(
                                text = "+ Tap to book",
                                style = MaterialTheme.typography.labelSmall,
                                color = IndigoLight
                            )
                        }
                    }
                }
            }
        }
    }

    // Quick Meeting Creation Bottom Sheet Modal
    CreateMeetingBottomSheet(
        isOpen = isCreateSheetOpen,
        onDismiss = { isCreateSheetOpen = false },
        viewModel = viewModel,
        initialDate = selectedDate
    )
    }
}
