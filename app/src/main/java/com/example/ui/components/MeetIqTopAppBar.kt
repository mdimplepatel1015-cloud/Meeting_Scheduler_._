package com.example.ui.components

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.AdminPanelSettings
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.User
import com.example.data.model.UserRole
import com.example.ui.theme.CyanAccent
import com.example.ui.theme.IndigoLight
import com.example.ui.theme.IndigoPrimary
import com.example.ui.theme.RoseError
import com.example.ui.theme.VioletAccent
import com.example.ui.viewmodel.AppScreen

@Composable
fun MeetIqTopAppBar(
    currentScreen: AppScreen,
    user: User?,
    unreadNotificationsCount: Int,
    onNavigate: (AppScreen) -> Unit,
    onBack: () -> Unit,
    onRoleChange: (UserRole) -> Unit
) {
    var showUserMenu by remember { mutableStateOf(false) }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left Brand or Back Button
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (currentScreen != AppScreen.DASHBOARD) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("top_bar_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    } else {
                        // Brand Icon
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(
                                    Brush.linearGradient(
                                        listOf(IndigoPrimary, VioletAccent)
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "MeetIQ Logo",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = when (currentScreen) {
                                    AppScreen.DASHBOARD -> "MeetIQ"
                                    AppScreen.CALENDAR -> "Calendar & Schedule"
                                    AppScreen.MEETINGS_LIST -> "All Meetings"
                                    AppScreen.MEETING_DETAIL -> "Meeting Intelligence"
                                    AppScreen.MEETING_CREATE -> "Schedule Meeting"
                                    AppScreen.LIVE_MEETING -> "Live Meeting Room"
                                    AppScreen.MEETING_PREPARATION -> "Meeting Preparation"
                                    AppScreen.ACTION_ITEMS -> "Action Items & Tasks"
                                    AppScreen.DECISION_LOG -> "Decision Tracker"
                                    AppScreen.ANALYTICS -> "Meeting Intelligence & Analytics"
                                    AppScreen.NOTIFICATIONS -> "Notification Center"
                                    AppScreen.ADMIN_PANEL -> "Admin Governance"
                                    AppScreen.USER_PROFILE -> "Profile & Preferences"
                                },
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (currentScreen == AppScreen.DASHBOARD) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(IndigoPrimary.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "AI OPS",
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = IndigoLight
                                    )
                                }
                            }
                        }
                        Text(
                            text = when (currentScreen) {
                                AppScreen.DASHBOARD -> "Intelligent Meeting Operating System"
                                AppScreen.LIVE_MEETING -> "Real-time AI Transcription & Notes"
                                AppScreen.MEETING_DETAIL -> "Summary • Transcripts • Health Score"
                                else -> "Enterprise Workspace"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Right Action Icons
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Schedule
                    if (currentScreen != AppScreen.MEETING_CREATE && currentScreen != AppScreen.LIVE_MEETING) {
                        IconButton(
                            onClick = { onNavigate(AppScreen.MEETING_CREATE) },
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(IndigoPrimary.copy(alpha = 0.15f))
                                .size(36.dp)
                                .testTag("top_bar_create_meeting_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New Meeting",
                                tint = IndigoLight,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    // Notifications Badge
                    IconButton(
                        onClick = { onNavigate(AppScreen.NOTIFICATIONS) },
                        modifier = Modifier.testTag("top_bar_notifications_button")
                    ) {
                        BadgedBox(
                            badge = {
                                if (unreadNotificationsCount > 0) {
                                    Badge(containerColor = RoseError) {
                                        Text(text = "$unreadNotificationsCount", fontSize = 10.sp)
                                    }
                                }
                            }
                        ) {
                            Icon(
                                imageVector = Icons.Default.Notifications,
                                contentDescription = "Notifications",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(4.dp))

                    // User Profile Avatar with Role Menu
                    Box {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(
                                    Brush.linearGradient(
                                        listOf(IndigoPrimary, CyanAccent)
                                    )
                                )
                                .clickable { showUserMenu = true }
                                .testTag("top_bar_user_profile_avatar"),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = user?.avatarInitials ?: "AM",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        DropdownMenu(
                            expanded = showUserMenu,
                            onDismissRequest = { showUserMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Text(
                                            text = user?.name ?: "Alex Morgan",
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                        Text(
                                            text = "Role: ${user?.role?.name ?: "MANAGER"}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = IndigoLight
                                        )
                                    }
                                },
                                onClick = {
                                    showUserMenu = false
                                    onNavigate(AppScreen.USER_PROFILE)
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.Person, contentDescription = null)
                                }
                            )

                            DropdownMenuItem(
                                text = { Text("Admin Panel") },
                                onClick = {
                                    showUserMenu = false
                                    onNavigate(AppScreen.ADMIN_PANEL)
                                },
                                leadingIcon = {
                                    Icon(Icons.Outlined.AdminPanelSettings, contentDescription = null)
                                }
                            )

                            // Quick Switch Role for Demo Testing
                            DropdownMenuItem(
                                text = { Text("Switch Role: Admin") },
                                onClick = {
                                    showUserMenu = false
                                    onRoleChange(UserRole.ADMIN)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Switch Role: Manager") },
                                onClick = {
                                    showUserMenu = false
                                    onRoleChange(UserRole.MANAGER)
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("Switch Role: Employee") },
                                onClick = {
                                    showUserMenu = false
                                    onRoleChange(UserRole.EMPLOYEE)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}
