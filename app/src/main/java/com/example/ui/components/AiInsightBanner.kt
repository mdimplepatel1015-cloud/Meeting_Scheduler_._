package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ElectricBolt
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.theme.AmberWarning
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseError
import com.example.ui.theme.VioletAccent

data class InsightCardItem(
    val title: String,
    val description: String,
    val recommendation: String,
    val icon: ImageVector,
    val accentColor: Color,
    val actionLabel: String
)

@Composable
fun AiInsightBanner(
    onActionClick: (String) -> Unit
) {
    val insights = listOf(
        InsightCardItem(
            title = "Peak Meeting Density Alert",
            description = "Wednesday has your highest meeting load (4.2 hours scheduled, 3 back-to-back).",
            recommendation = "AI Suggestion: Insert a 15-minute recovery buffer between 2:00 PM and 3:30 PM.",
            icon = Icons.Default.Schedule,
            accentColor = AmberWarning,
            actionLabel = "Add 15m Buffer"
        ),
        InsightCardItem(
            title = "Recurring Meeting Optimization",
            description = "'Weekly Architecture Alignment' consistently finishes 25 minutes early with 100% agenda items cleared.",
            recommendation = "AI Suggestion: Reduce scheduled duration from 60 min to 30 min to reclaim 2.0 hrs/month.",
            icon = Icons.Default.TrendingUp,
            accentColor = CyanAccent,
            actionLabel = "Optimize Meeting (30m)"
        ),
        InsightCardItem(
            title = "Overdue Action Item Follow-up",
            description = "'Finalize enterprise SLA legal agreement' was due yesterday from the Product Launch session.",
            recommendation = "AI Suggestion: Send automated Slack nudge to Rahul and legal reviewer.",
            icon = Icons.Default.Warning,
            accentColor = RoseError,
            actionLabel = "Send Nudge"
        ),
        InsightCardItem(
            title = "High Team Engagement Milestone",
            description = "Your recent Q3 AI Platform Architecture session scored a 92/100 Health Score with 96% topic alignment.",
            recommendation = "AI Insight: Balanced speaking distribution observed across Engineering, DevOps, and Product.",
            icon = Icons.Default.AutoAwesome,
            accentColor = EmeraldSuccess,
            actionLabel = "View Health Breakdown"
        )
    )

    var currentIndex by remember { mutableIntStateOf(0) }
    val current = insights[currentIndex]

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .border(
                1.dp,
                Brush.horizontalGradient(
                    listOf(current.accentColor.copy(alpha = 0.5f), IndigoPrimary.copy(alpha = 0.3f))
                ),
                RoundedCornerShape(16.dp)
            )
            .testTag("ai_insight_banner"),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(current.accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = current.icon,
                            contentDescription = null,
                            tint = current.accentColor,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "MEETIQ AI INSIGHT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = current.accentColor,
                        letterSpacing = 1.sp
                    )
                }

                // Carousel indicator dots
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    insights.forEachIndexed { index, _ ->
                        Box(
                            modifier = Modifier
                                .size(if (index == currentIndex) 16.dp else 6.dp, 6.dp)
                                .clip(RoundedCornerShape(3.dp))
                                .background(
                                    if (index == currentIndex) current.accentColor else MaterialTheme.colorScheme.outline.copy(
                                        alpha = 0.4f
                                    )
                                )
                                .clickable { currentIndex = index }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = current.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = current.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.7f))
                    .padding(10.dp)
            ) {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = IndigoLight,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = current.recommendation,
                        style = MaterialTheme.typography.bodySmall,
                        color = IndigoLight,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(
                    onClick = { onActionClick(current.actionLabel) },
                    modifier = Modifier.testTag("ai_insight_action_button"),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = current.accentColor
                    )
                ) {
                    Text(text = current.actionLabel, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                Text(
                    text = "Swipe/Tap dots for more (${currentIndex + 1}/${insights.size})",
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
