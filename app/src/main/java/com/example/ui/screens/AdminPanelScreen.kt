package com.example.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.VioletAccent
import com.example.ui.viewmodel.MeetingViewModel

@Composable
fun AdminPanelScreen(
    viewModel: MeetingViewModel
) {
    val auditLogs by viewModel.allAuditLogs.collectAsState()
    var googleCalendarSync by remember { mutableStateOf(true) }
    var microsoft365Sync by remember { mutableStateOf(true) }
    var slackBotIntegration by remember { mutableStateOf(true) }
    var enforceRecordingConsent by remember { mutableStateOf(true) }
    var autoRedactPII by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("admin_panel_screen_root"),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 80.dp)
    ) {
        item {
            Text(
                text = "Enterprise Workspace & Security",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "Role-based permissions, governance policies & real-time compliance audit trail",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Integrations Status
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Enterprise Connectors & Sync", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(10.dp))

                    ConnectorToggleRow(
                        title = "Google Workspace & Meet",
                        subtitle = "Real-time calendar two-way sync active",
                        checked = googleCalendarSync,
                        onCheckedChange = { googleCalendarSync = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ConnectorToggleRow(
                        title = "Microsoft 365 & Teams",
                        subtitle = "Enterprise tenant federation enabled",
                        checked = microsoft365Sync,
                        onCheckedChange = { microsoft365Sync = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ConnectorToggleRow(
                        title = "Slack & Discord Workspaces",
                        subtitle = "MeetIQ AI instant briefing bot connected",
                        checked = slackBotIntegration,
                        onCheckedChange = { slackBotIntegration = it }
                    )
                }
            }
        }

        // Security & Privacy Controls
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f)),
                shape = RoundedCornerShape(14.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("Privacy & Governance Rules", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleSmall)
                    Spacer(modifier = Modifier.height(10.dp))

                    ConnectorToggleRow(
                        title = "Enforce Recording & Transcription Consent",
                        subtitle = "Notify all participants upon entering live room",
                        checked = enforceRecordingConsent,
                        onCheckedChange = { enforceRecordingConsent = it }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    ConnectorToggleRow(
                        title = "Automated PII Redaction in Transcripts",
                        subtitle = "Mask credit cards, SSNs, and passwords using AI",
                        checked = autoRedactPII,
                        onCheckedChange = { autoRedactPII = it }
                    )
                }
            }
        }

        // Audit Logs
        item {
            Text(
                text = "Security Audit Trail (${auditLogs.size} events)",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )
        }

        items(auditLogs) { log ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp)),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            ) {
                Row(
                    modifier = Modifier.padding(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(IndigoPrimary.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, tint = IndigoLight, modifier = Modifier.size(14.dp))
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "${log.actorName} • ${log.action}",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${log.details} (${log.timestamp})",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConnectorToggleRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(text = title, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)
            Text(text = subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = IndigoPrimary,
                checkedTrackColor = IndigoLight.copy(alpha = 0.3f)
            )
        )
    }
}
