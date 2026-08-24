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
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Gavel
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ActionItemStatus
import com.example.data.model.MeetingStatus
import com.example.ui.components.ActionItemCard
import com.example.ui.components.AiInsightBanner
import com.example.ui.components.MeetingCard
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
fun DashboardScreen(
    viewModel: MeetingViewModel,
    onOpenAiCopilot: () -> Unit
) {
    val user by viewModel.currentUser.collectAsState()
    val meetings by viewModel.allMeetings.collectAsState()
    val actionItems by viewModel.allActionItems.collectAsState()
    val decisions by viewModel.allDecisions.collectAsState()
    val productivityMetrics by viewModel.productivityMetrics.collectAsState()

    val todayMeetings = meetings.filter { it.date == "2026-08-23" || it.status == MeetingStatus.UPCOMING }
    val pendingActionItems = actionItems.filter { it.status != ActionItemStatus.COMPLETED }
    val overdueActionItems = actionItems.filter { it.status == ActionItemStatus.OVERDUE }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("dashboard_screen_root"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        // Welcome Header & Quick AI Action
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Good morning, ${user?.name?.substringBefore(" ") ?: "Alex"} 👋",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "You have ${todayMeetings.size} meetings scheduled today • ${pendingActionItems.size} pending tasks",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Button(
                    onClick = onOpenAiCopilot,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = IndigoPrimary
                    ),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.testTag("dashboard_ask_ai_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Ask AI", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // AI Insight Banner Carousel
        item {
            AiInsightBanner(
                onActionClick = { action ->
                    when {
                        action.contains("Optimize", ignoreCase = true) -> viewModel.navigateTo(AppScreen.ANALYTICS)
                        action.contains("Health", ignoreCase = true) -> viewModel.navigateTo(AppScreen.MEETING_DETAIL, "meet_1")
                        action.contains("Nudge", ignoreCase = true) -> viewModel.navigateTo(AppScreen.ACTION_ITEMS)
                        else -> onOpenAiCopilot()
                    }
                }
            )
        }

        // Quick Stats Summary Cards Grid
        item {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(
                        title = "Meetings Today",
                        value = "${todayMeetings.size}",
                        subtext = "2 require prep",
                        icon = Icons.Default.Groups,
                        color = IndigoLight,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.MEETINGS_LIST) }
                    )
                    MetricBox(
                        title = "Meeting Hours",
                        value = "3.2h",
                        subtext = "Healthy workload",
                        icon = Icons.Default.Schedule,
                        color = CyanAccent,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.ANALYTICS) }
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MetricBox(
                        title = "Action Items",
                        value = "${pendingActionItems.size}",
                        subtext = if (overdueActionItems.isNotEmpty()) "${overdueActionItems.size} overdue" else "All on track",
                        icon = Icons.Default.CheckCircle,
                        color = if (overdueActionItems.isNotEmpty()) RoseError else EmeraldSuccess,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.ACTION_ITEMS) }
                    )
                    MetricBox(
                        title = "Avg Health Score",
                        value = "88%",
                        subtext = "+4% this month",
                        icon = Icons.Default.TrendingUp,
                        color = VioletAccent,
                        modifier = Modifier.weight(1f),
                        onClick = { viewModel.navigateTo(AppScreen.ANALYTICS) }
                    )
                }
            }
        }

        // Fast Action Buttons Row
        item {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                item {
                    FastActionButton(
                        label = "Schedule Meeting",
                        icon = Icons.Default.Add,
                        color = IndigoPrimary,
                        onClick = { viewModel.navigateTo(AppScreen.MEETING_CREATE) }
                    )
                }
                item {
                    FastActionButton(
                        label = "Smart Calendar",
                        icon = Icons.Default.CalendarMonth,
                        color = CyanAccent,
                        onClick = { viewModel.navigateTo(AppScreen.CALENDAR) }
                    )
                }
                item {
                    FastActionButton(
                        label = "Decision Log",
                        icon = Icons.Default.Gavel,
                        color = VioletAccent,
                        onClick = { viewModel.navigateTo(AppScreen.DECISION_LOG) }
                    )
                }
                item {
                    FastActionButton(
                        label = "Productivity Insights",
                        icon = Icons.Default.Analytics,
                        color = EmeraldSuccess,
                        onClick = { viewModel.navigateTo(AppScreen.ANALYTICS) }
                    )
                }
            }
        }

        // Today's Agenda & Meetings Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Today's Schedule & Intelligence",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "See All",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = IndigoLight,
                    modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.MEETINGS_LIST) }
                )
            }
        }

        // Meetings List
        items(todayMeetings.take(4)) { meeting ->
            MeetingCard(
                meeting = meeting,
                onClick = { viewModel.selectMeeting(meeting.id) },
                onJoinLive = { viewModel.startLiveMeeting(meeting.id) },
                onPrepare = { viewModel.navigateTo(AppScreen.MEETING_PREPARATION, meeting.id) }
            )
        }

        // Priority Action Items Section
        item {
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Assigned Action Items",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = "View All (${actionItems.size})",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = IndigoLight,
                    modifier = Modifier.clickable { viewModel.navigateTo(AppScreen.ACTION_ITEMS) }
                )
            }
        }

        items(pendingActionItems.take(3)) { item ->
            ActionItemCard(
                item = item,
                onStatusToggle = { newStatus ->
                    viewModel.updateActionItemStatus(item.id, newStatus)
                }
            )
        }
    }
}

@Composable
private fun MetricBox(
    title: String,
    value: String,
    subtext: String,
    icon: ImageVector,
    color: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = color,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun FastActionButton(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(color.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = color,
                    modifier = Modifier.size(14.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}
