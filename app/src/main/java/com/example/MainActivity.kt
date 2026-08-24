package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.components.FloatingAiAssistantDialog
import com.example.ui.components.MeetIqBottomNav
import com.example.ui.components.MeetIqTopAppBar
import com.example.ui.screens.ActionItemsScreen
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.AnalyticsScreen
import com.example.ui.screens.CalendarScreen
import com.example.ui.screens.DashboardScreen
import com.example.ui.screens.DecisionLogScreen
import com.example.ui.screens.LiveMeetingScreen
import com.example.ui.screens.MeetingCreateScreen
import com.example.ui.screens.MeetingDetailScreen
import com.example.ui.screens.MeetingPreparationScreen
import com.example.ui.screens.MeetingsListScreen
import com.example.ui.screens.NotificationsScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.theme.MeetIqTheme
import com.example.ui.viewmodel.AiCopilotViewModel
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MeetingViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            MeetIqTheme {
                val meetingViewModel: MeetingViewModel = viewModel()
                val aiCopilotViewModel: AiCopilotViewModel = viewModel()

                MeetIqApp(
                    meetingViewModel = meetingViewModel,
                    aiCopilotViewModel = aiCopilotViewModel
                )
            }
        }
    }
}

@Composable
fun MeetIqApp(
    meetingViewModel: MeetingViewModel,
    aiCopilotViewModel: AiCopilotViewModel
) {
    val currentScreen by meetingViewModel.currentScreen.collectAsState()
    val unreadNotifications by meetingViewModel.unreadNotificationCount.collectAsState()
    val currentUser by meetingViewModel.currentUser.collectAsState()

    // Intercept back button when not on dashboard
    BackHandler(enabled = currentScreen != AppScreen.DASHBOARD) {
        if (currentScreen == AppScreen.LIVE_MEETING) {
            meetingViewModel.stopLiveMeeting()
            meetingViewModel.navigateTo(AppScreen.MEETING_DETAIL)
        } else {
            meetingViewModel.navigateBack()
        }
    }

    val showBottomNav = currentScreen in listOf(
        AppScreen.DASHBOARD,
        AppScreen.CALENDAR,
        AppScreen.MEETINGS_LIST,
        AppScreen.ACTION_ITEMS,
        AppScreen.DECISION_LOG,
        AppScreen.ANALYTICS
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            if (currentScreen != AppScreen.LIVE_MEETING) {
                MeetIqTopAppBar(
                    currentScreen = currentScreen,
                    user = currentUser,
                    unreadNotificationsCount = unreadNotifications,
                    onNavigate = { screen -> meetingViewModel.navigateTo(screen) },
                    onBack = { meetingViewModel.navigateBack() },
                    onRoleChange = { role -> meetingViewModel.switchUserRole(role) }
                )
            }
        },
        bottomBar = {
            if (showBottomNav) {
                MeetIqBottomNav(
                    currentScreen = currentScreen,
                    onNavigate = { screen -> meetingViewModel.navigateTo(screen) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "ScreenTransition"
            ) { screen ->
                when (screen) {
                    AppScreen.DASHBOARD -> DashboardScreen(
                        viewModel = meetingViewModel,
                        onOpenAiCopilot = { aiCopilotViewModel.toggleOpen(true) }
                    )
                    AppScreen.CALENDAR -> CalendarScreen(
                        viewModel = meetingViewModel
                    )
                    AppScreen.MEETINGS_LIST -> MeetingsListScreen(
                        viewModel = meetingViewModel
                    )
                    AppScreen.MEETING_DETAIL -> MeetingDetailScreen(
                        viewModel = meetingViewModel
                    )
                    AppScreen.MEETING_CREATE -> MeetingCreateScreen(
                        viewModel = meetingViewModel
                    )
                    AppScreen.LIVE_MEETING -> LiveMeetingScreen(
                        viewModel = meetingViewModel
                    )
                    AppScreen.MEETING_PREPARATION -> MeetingPreparationScreen(
                        viewModel = meetingViewModel
                    )
                    AppScreen.ACTION_ITEMS -> ActionItemsScreen(
                        viewModel = meetingViewModel
                    )
                    AppScreen.DECISION_LOG -> DecisionLogScreen(
                        viewModel = meetingViewModel
                    )
                    AppScreen.ANALYTICS -> AnalyticsScreen(
                        viewModel = meetingViewModel
                    )
                    AppScreen.NOTIFICATIONS -> NotificationsScreen(
                        viewModel = meetingViewModel
                    )
                    AppScreen.ADMIN_PANEL -> AdminPanelScreen(
                        viewModel = meetingViewModel
                    )
                    AppScreen.USER_PROFILE -> UserProfileScreen(
                        viewModel = meetingViewModel
                    )
                }
            }
        }

        // Global AI Copilot Assistant Dialog
        FloatingAiAssistantDialog(
            viewModel = aiCopilotViewModel,
            onNavigate = { screen -> meetingViewModel.navigateTo(screen) }
        )
    }
}
