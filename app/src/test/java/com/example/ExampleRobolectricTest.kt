package com.example

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.model.ActionItemStatus
import com.example.data.model.MeetingPriority
import com.example.data.model.MeetingRecurrence
import com.example.data.model.MeetingType
import com.example.data.model.MeetingVisibility
import com.example.ui.viewmodel.AppScreen
import com.example.ui.viewmodel.MeetingViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    private lateinit var app: Application
    private lateinit var viewModel: MeetingViewModel

    @Before
    fun setUp() {
        app = ApplicationProvider.getApplicationContext()
        viewModel = MeetingViewModel(app)
    }

    @Test
    fun `test app name resource is MeetIQ`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("MeetIQ", appName)
    }

    @Test
    fun `test database seeding and meetings stream`() = runBlocking {
        // Allow init to finish
        val meetings = viewModel.allMeetings.first { it.isNotEmpty() }
        assertTrue("Expected seeded meetings in database", meetings.isNotEmpty())

        val firstMeeting = meetings.first()
        assertNotNull(firstMeeting.title)
    }

    @Test
    fun `test action items and decision tracking`() = runBlocking {
        val actionItems = viewModel.allActionItems.first { it.isNotEmpty() }
        assertTrue("Expected seeded action items", actionItems.isNotEmpty())

        val decisions = viewModel.allDecisions.first { it.isNotEmpty() }
        assertTrue("Expected seeded decisions", decisions.isNotEmpty())
    }

    @Test
    fun `test navigation state changes`() {
        assertEquals(AppScreen.DASHBOARD, viewModel.currentScreen.value)
        viewModel.navigateTo(AppScreen.CALENDAR)
        assertEquals(AppScreen.CALENDAR, viewModel.currentScreen.value)
        viewModel.navigateTo(AppScreen.ANALYTICS)
        assertEquals(AppScreen.ANALYTICS, viewModel.currentScreen.value)
        viewModel.navigateBack()
        assertEquals(AppScreen.DASHBOARD, viewModel.currentScreen.value)
    }
}
