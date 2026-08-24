package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Loop
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Meeting
import com.example.data.model.MeetingPriority
import com.example.data.model.MeetingStatus
import com.example.data.model.MeetingSummary
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseError
import com.example.ui.theme.VioletAccent

@Composable
fun MeetingCard(
    meeting: Meeting,
    summary: MeetingSummary? = null,
    onClick: () -> Unit,
    onJoinLive: () -> Unit,
    onPrepare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .testTag("meeting_card_${meeting.id}"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row: Category, Recurrence & Status Badges
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(IndigoPrimary.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            text = meeting.category.uppercase(),
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = IndigoLight
                        )
                    }

                    if (meeting.isRecurring) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CyanAccent.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 3.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Loop,
                                    contentDescription = null,
                                    tint = CyanAccent,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(3.dp))
                                Text(
                                    text = meeting.recurrence.name.lowercase().replaceFirstChar { it.uppercase() },
                                    fontSize = 10.sp,
                                    color = CyanAccent,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
                }

                // Status Badge
                val (statusColor, statusBg, statusText) = when (meeting.status) {
                    MeetingStatus.UPCOMING -> Triple(CyanAccent, CyanAccent.copy(alpha = 0.15f), "UPCOMING")
                    MeetingStatus.IN_PROGRESS -> Triple(EmeraldSuccess, EmeraldSuccess.copy(alpha = 0.2f), "LIVE NOW")
                    MeetingStatus.COMPLETED -> Triple(MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surface, "COMPLETED")
                    MeetingStatus.CANCELLED -> Triple(RoseError, RoseError.copy(alpha = 0.15f), "CANCELLED")
                    MeetingStatus.RESCHEDULED -> Triple(AmberWarning, AmberWarning.copy(alpha = 0.15f), "RESCHEDULED")
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(statusBg)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = statusText,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Meeting Title
            Text(
                text = meeting.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(6.dp))

            // Time & Location Info
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AccessTime,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${meeting.date} • ${meeting.startTime} – ${meeting.endTime} (${meeting.durationMinutes}m)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = meeting.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (meeting.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    meeting.tags.take(3).forEach { tag ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(MaterialTheme.colorScheme.surface)
                                .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 7.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "#$tag",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium,
                                color = IndigoLight
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1-Sentence AI-Generated Summary Preview Card
            val aiSummarySentence = remember(meeting, summary) {
                when {
                    summary != null -> {
                        val raw = summary.objective.ifBlank {
                            summary.keyDiscussionPoints.lines().firstOrNull { it.isNotBlank() } ?: summary.decisionsSummary
                        }
                        val cleaned = raw.trim().removePrefix("•").removePrefix("-").trim()
                        val firstSentence = cleaned.split(". ").firstOrNull() ?: cleaned
                        if (firstSentence.endsWith(".")) firstSentence else "$firstSentence."
                    }
                    !meeting.preparationBriefing.isNullOrBlank() -> {
                        val first = meeting.preparationBriefing.split(". ").firstOrNull() ?: meeting.preparationBriefing
                        if (first.endsWith(".")) first else "$first."
                    }
                    meeting.description.isNotBlank() -> {
                        val first = meeting.description.split(". ").firstOrNull() ?: meeting.description
                        if (first.endsWith(".")) first else "$first."
                    }
                    else -> {
                        "Strategic ${meeting.category.lowercase()} meeting focusing on key deliverables and attendee alignment."
                    }
                }
            }

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .border(1.dp, IndigoPrimary.copy(alpha = 0.25f), RoundedCornerShape(10.dp))
                    .testTag("meeting_summary_preview_${meeting.id}"),
                color = IndigoPrimary.copy(alpha = 0.08f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 10.dp, vertical = 7.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "AI Summary Preview",
                        tint = CyanAccent,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(7.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "AI SUMMARY PREVIEW",
                                fontSize = 8.5.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = CyanAccent,
                                letterSpacing = 0.5.sp
                            )
                        }
                        Spacer(modifier = Modifier.height(1.dp))
                        Text(
                            text = aiSummarySentence,
                            style = MaterialTheme.typography.bodySmall,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.85f),
                            lineHeight = 15.sp,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Bottom Badges & Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Priority & Health Score Tag
                Row(verticalAlignment = Alignment.CenterVertically) {
                    val priorityColor = when (meeting.priority) {
                        MeetingPriority.HIGH -> RoseError
                        MeetingPriority.MEDIUM -> AmberWarning
                        MeetingPriority.LOW -> EmeraldSuccess
                    }
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(priorityColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "${meeting.priority.name} Priority",
                        fontSize = 11.sp,
                        color = priorityColor,
                        fontWeight = FontWeight.Medium
                    )

                    if (meeting.status == MeetingStatus.COMPLETED) {
                        Spacer(modifier = Modifier.width(10.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(EmeraldSuccess.copy(alpha = 0.15f))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Health: ${meeting.healthScore}/100",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSuccess
                            )
                        }
                    }
                }

                // Action Buttons
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (meeting.status == MeetingStatus.UPCOMING) {
                        OutlinedButton(
                            onClick = onPrepare,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("meeting_card_prepare_${meeting.id}"),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = IndigoLight
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Prepare", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }

                        Button(
                            onClick = onJoinLive,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("meeting_card_join_${meeting.id}"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = IndigoPrimary
                            )
                        ) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = null,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Join Room", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    } else {
                        OutlinedButton(
                            onClick = onClick,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.testTag("meeting_card_view_${meeting.id}")
                        ) {
                            Text("View Intelligence", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}
