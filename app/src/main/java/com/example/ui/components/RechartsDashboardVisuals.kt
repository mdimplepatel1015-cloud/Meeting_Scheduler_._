package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.QueryBuilder
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseError
import com.example.ui.theme.VioletAccent

data class DurationDataPoint(
    val label: String,
    val durationMinutes: Float,
    val targetMinutes: Float = 30f,
    val meetingCount: Int
)

data class ActionItemFrequencyPoint(
    val meetingName: String,
    val totalItems: Int,
    val completedItems: Int,
    val inProgressItems: Int
)

data class ParticipantEngagementMetric(
    val name: String,
    val role: String,
    val speakingSharePercent: Int,
    val attendanceRate: Int,
    val actionItemsResolved: Int,
    val avatarColor: Color
)

@Composable
fun MeetingDurationTrendsChart(
    modifier: Modifier = Modifier
) {
    val data = listOf(
        DurationDataPoint("Mon", 45f, 30f, 3),
        DurationDataPoint("Tue", 60f, 30f, 4),
        DurationDataPoint("Wed", 30f, 30f, 2),
        DurationDataPoint("Thu", 75f, 30f, 5),
        DurationDataPoint("Fri", 35f, 30f, 2),
        DurationDataPoint("Sat", 15f, 30f, 1)
    )

    var selectedIndex by remember { mutableIntStateOf(3) }
    val maxDuration = 90f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("meeting_duration_trends_chart"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QueryBuilder,
                            contentDescription = null,
                            tint = IndigoLight,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Meeting Duration Trends",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Daily session lengths vs 30m target",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Interactive selected pill
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = IndigoPrimary.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "${data[selectedIndex].label}: ${data[selectedIndex].durationMinutes.toInt()}m (${data[selectedIndex].meetingCount} syncs)",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = IndigoLight
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Recharts-style Interactive Bar & Area Canvas
            val primaryColor = IndigoPrimary
            val accentColor = CyanAccent

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val width = size.width
                    val height = size.height
                    val barWidth = width / (data.size * 2f)
                    val spacing = width / data.size

                    // Draw 30m Target Guideline
                    val targetY = height - (30f / maxDuration) * height
                    drawLine(
                        color = Color(0xFF64748B),
                        start = Offset(0f, targetY),
                        end = Offset(width, targetY),
                        strokeWidth = 2f,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                    )

                    // Draw Area Trend line behind bars
                    val trendPath = Path()
                    data.forEachIndexed { index, point ->
                        val x = index * spacing + spacing / 2f
                        val y = height - (point.durationMinutes / maxDuration) * (height - 20f)
                        if (index == 0) trendPath.moveTo(x, y) else trendPath.lineTo(x, y)
                    }
                    drawPath(
                        path = trendPath,
                        color = CyanAccent.copy(alpha = 0.3f),
                        style = Stroke(width = 3f)
                    )

                    // Draw Bars
                    data.forEachIndexed { index, point ->
                        val x = index * spacing + (spacing - barWidth) / 2f
                        val barHeight = (point.durationMinutes / maxDuration) * (height - 20f)
                        val y = height - barHeight
                        val isSelected = index == selectedIndex

                        val barBrush = if (isSelected) {
                            Brush.verticalGradient(listOf(CyanAccent, IndigoPrimary))
                        } else {
                            Brush.verticalGradient(listOf(IndigoLight.copy(alpha = 0.6f), IndigoPrimary.copy(alpha = 0.4f)))
                        }

                        drawRoundRect(
                            brush = barBrush,
                            topLeft = Offset(x, y),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(8f, 8f)
                        )
                    }
                }
            }

            // X-Axis Day Labels
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                data.forEachIndexed { index, point ->
                    val isSelected = index == selectedIndex
                    Text(
                        text = point.label,
                        fontSize = 11.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) IndigoLight else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { selectedIndex = index }
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun ActionItemFrequencyChart(
    modifier: Modifier = Modifier
) {
    val items = listOf(
        ActionItemFrequencyPoint("Architecture Sync", 5, 4, 1),
        ActionItemFrequencyPoint("Sprint Planning", 8, 6, 2),
        ActionItemFrequencyPoint("Security Audit", 4, 3, 1),
        ActionItemFrequencyPoint("Product Backlog", 6, 5, 1),
        ActionItemFrequencyPoint("Executive 1-on-1", 3, 3, 0)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("action_item_frequency_chart"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(EmeraldSuccess.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.BarChart,
                            contentDescription = null,
                            tint = EmeraldSuccess,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Action Items Frequency per Meeting",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Generated vs Resolved Follow-through",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(EmeraldSuccess))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Done", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(CyanAccent))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("In Progress", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items.forEach { point ->
                    val resolvedRatio = point.completedItems.toFloat() / point.totalItems
                    val progressAnim by animateFloatAsState(
                        targetValue = resolvedRatio,
                        animationSpec = tween(durationMillis = 800),
                        label = "progress"
                    )

                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = point.meetingName,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "${point.completedItems}/${point.totalItems} items (${(resolvedRatio * 100).toInt()}%)",
                                fontSize = 11.sp,
                                color = EmeraldSuccess,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surface)
                        ) {
                            // Completed segment
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth(fraction = progressAnim)
                                    .height(8.dp)
                                    .background(EmeraldSuccess)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ParticipantEngagementMatrixChart(
    modifier: Modifier = Modifier
) {
    val participants = listOf(
        ParticipantEngagementMetric("Alex Morgan", "TPM / Host", 34, 100, 8, IndigoLight),
        ParticipantEngagementMetric("Rahul Sharma", "Lead Architect", 28, 96, 6, CyanAccent),
        ParticipantEngagementMetric("Priya Patel", "Product Lead", 22, 92, 5, VioletAccent),
        ParticipantEngagementMetric("Sarah Jenkins", "DevOps Eng", 16, 95, 4, EmeraldSuccess)
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("participant_engagement_chart"),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(VioletAccent.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Group,
                            contentDescription = null,
                            tint = VioletAccent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Participant Engagement Matrix",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Speaking time distribution & attendance score",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = EmeraldSuccess.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = "96% High Engagement",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = EmeraldSuccess
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Multi-segment Speaking Share Stacked Bar
            Column {
                Text(
                    text = "Speaking Time Share (%)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    participants.forEach { p ->
                        Box(
                            modifier = Modifier
                                .weight(p.speakingSharePercent.toFloat())
                                .height(12.dp)
                                .background(p.avatarColor)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Participant rows
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                participants.forEach { p ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(p.avatarColor)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = p.name,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = p.role,
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "${p.speakingSharePercent}% talk time",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = p.avatarColor
                                )
                                Text(
                                    text = "${p.attendanceRate}% attendance",
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
