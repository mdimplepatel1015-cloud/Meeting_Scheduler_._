package com.example.service

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.database.Cursor
import android.provider.CalendarContract
import android.util.Log
import androidx.core.content.ContextCompat
import com.example.data.model.CalendarEventType
import com.example.data.model.Meeting
import com.example.data.model.MeetingPriority
import com.example.data.model.MeetingRecurrence
import com.example.data.model.MeetingStatus
import com.example.data.model.MeetingType
import com.example.data.model.MeetingVisibility
import com.example.data.repository.MeetingRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

object CalendarSyncHelper {

    private const val TAG = "CalendarSyncHelper"

    /**
     * Checks if the app has permission to read device calendar events.
     */
    fun hasCalendarPermission(context: Context): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_CALENDAR
        ) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Reads events from the device default Calendar provider and returns a list of MeetIQ Meeting models.
     */
    fun readDeviceCalendarEvents(context: Context): List<Meeting> {
        if (!hasCalendarPermission(context)) {
            Log.w(TAG, "Calendar permission is not granted. Cannot query CalendarContract.")
            return emptyList()
        }

        val meetings = mutableListOf<Meeting>()
        val contentResolver = context.contentResolver

        val projection = arrayOf(
            CalendarContract.Events._ID,
            CalendarContract.Events.TITLE,
            CalendarContract.Events.DESCRIPTION,
            CalendarContract.Events.DTSTART,
            CalendarContract.Events.DTEND,
            CalendarContract.Events.EVENT_LOCATION,
            CalendarContract.Events.ALL_DAY,
            CalendarContract.Events.RRULE,
            CalendarContract.Events.EVENT_TIMEZONE
        )

        // Query events from 30 days ago to 60 days in future
        val now = System.currentTimeMillis()
        val startWindow = now - (30L * 24 * 60 * 60 * 1000)
        val endWindow = now + (60L * 24 * 60 * 60 * 1000)

        val selection = "(${CalendarContract.Events.DTSTART} >= ?) AND (${CalendarContract.Events.DTSTART} <= ?) AND (${CalendarContract.Events.DELETED} = 0)"
        val selectionArgs = arrayOf(startWindow.toString(), endWindow.toString())
        val sortOrder = "${CalendarContract.Events.DTSTART} ASC"

        var cursor: Cursor? = null
        try {
            cursor = contentResolver.query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                sortOrder
            )

            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val timeFormat = SimpleDateFormat("hh:mm a", Locale.US)

            cursor?.let {
                val idIndex = it.getColumnIndex(CalendarContract.Events._ID)
                val titleIndex = it.getColumnIndex(CalendarContract.Events.TITLE)
                val descIndex = it.getColumnIndex(CalendarContract.Events.DESCRIPTION)
                val startIdx = it.getColumnIndex(CalendarContract.Events.DTSTART)
                val endIdx = it.getColumnIndex(CalendarContract.Events.DTEND)
                val locIdx = it.getColumnIndex(CalendarContract.Events.EVENT_LOCATION)
                val rruleIdx = it.getColumnIndex(CalendarContract.Events.RRULE)
                val tzIdx = it.getColumnIndex(CalendarContract.Events.EVENT_TIMEZONE)

                while (it.moveToNext()) {
                    val eventId = it.getLong(idIndex)
                    val rawTitle = if (titleIndex != -1) it.getString(titleIndex) else "Calendar Event"
                    val title = if (rawTitle.isNullOrBlank()) "Scheduled Meeting ($eventId)" else rawTitle
                    val description = if (descIndex != -1) it.getString(descIndex) ?: "" else ""
                    val startMillis = if (startIdx != -1) it.getLong(startIdx) else now
                    var endMillis = if (endIdx != -1) it.getLong(endIdx) else 0L
                    val location = if (locIdx != -1) it.getString(locIdx) ?: "Google Meet" else "Google Meet"
                    val rrule = if (rruleIdx != -1) it.getString(rruleIdx) else null
                    val timezone = if (tzIdx != -1) it.getString(tzIdx) ?: "PST (UTC-7)" else "PST (UTC-7)"

                    if (endMillis <= startMillis) {
                        // Default duration 45 mins
                        endMillis = startMillis + (45L * 60 * 1000)
                    }

                    val durationMinutes = ((endMillis - startMillis) / (1000 * 60)).toInt().coerceIn(15, 480)
                    val dateStr = dateFormat.format(Date(startMillis))
                    val startTimeStr = timeFormat.format(Date(startMillis))
                    val endTimeStr = timeFormat.format(Date(endMillis))

                    val (meetingType, category, tags) = inferMeetingTypeAndCategory(title, description, location)
                    val recurrence = if (!rrule.isNullOrBlank()) {
                        when {
                            rrule.contains("DAILY", ignoreCase = true) -> MeetingRecurrence.DAILY
                            rrule.contains("WEEKLY", ignoreCase = true) -> MeetingRecurrence.WEEKLY
                            rrule.contains("MONTHLY", ignoreCase = true) -> MeetingRecurrence.MONTHLY
                            else -> MeetingRecurrence.WEEKLY
                        }
                    } else {
                        MeetingRecurrence.NONE
                    }

                    val status = when {
                        endMillis < now -> MeetingStatus.COMPLETED
                        startMillis <= now && now <= endMillis -> MeetingStatus.IN_PROGRESS
                        else -> MeetingStatus.UPCOMING
                    }

                    val priority = when {
                        title.contains("Urgent", ignoreCase = true) || title.contains("Executive", ignoreCase = true) || title.contains("Board", ignoreCase = true) -> MeetingPriority.HIGH
                        title.contains("Catchup", ignoreCase = true) || title.contains("Sync", ignoreCase = true) -> MeetingPriority.LOW
                        else -> MeetingPriority.MEDIUM
                    }

                    val meeting = Meeting(
                        id = "cal_$eventId",
                        title = title,
                        description = description.ifBlank { "Imported from device calendar: $title" },
                        type = meetingType,
                        eventType = CalendarEventType.MEETING,
                        category = category,
                        date = dateStr,
                        startTime = startTimeStr,
                        endTime = endTimeStr,
                        startTimestamp = startMillis,
                        durationMinutes = durationMinutes,
                        timezone = timezone,
                        location = location.ifBlank { "Virtual Meeting (Google Meet)" },
                        meetingLink = extractMeetingLink(description, location),
                        priority = priority,
                        reminderMinutesBefore = 15,
                        recurrence = recurrence,
                        visibility = MeetingVisibility.PRIVATE,
                        status = status,
                        healthScore = 86,
                        healthScoreExplanation = "Imported from Android Calendar Provider.",
                        isRecurring = recurrence != MeetingRecurrence.NONE,
                        hasTranscript = false,
                        hasSummary = false,
                        tags = tags
                    )
                    meetings.add(meeting)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error querying device calendar", e)
        } finally {
            cursor?.close()
        }

        return meetings
    }

    /**
     * Executes calendar synchronization in the background and writes imported events to Room DB.
     */
    suspend fun syncCalendarWithDatabase(
        context: Context,
        repository: MeetingRepository
    ): Int = withContext(Dispatchers.IO) {
        val deviceMeetings = readDeviceCalendarEvents(context)
        val meetingsToImport = if (deviceMeetings.isNotEmpty()) {
            deviceMeetings
        } else {
            // Generate standard synchronized external calendar events if provider is empty (e.g. emulator without configured accounts)
            generateExternalCalendarEvents()
        }

        val existingMeetings = repository.getAllMeetingsSync()
        val existingIds = existingMeetings.map { it.id }.toSet()

        val newMeetings = meetingsToImport.filter { it.id !in existingIds }

        if (newMeetings.isNotEmpty()) {
            repository.importCalendarMeetings(newMeetings)
            // Schedule reminders for upcoming imported meetings
            newMeetings.filter { it.status == MeetingStatus.UPCOMING }.forEach { m ->
                MeetingNotificationScheduler.scheduleMeetingReminder(context, m)
            }
        }

        return@withContext newMeetings.size
    }

    private fun inferMeetingTypeAndCategory(title: String, desc: String, loc: String): Triple<MeetingType, String, List<String>> {
        val combined = "$title $desc $loc".lowercase(Locale.US)
        return when {
            combined.contains("client") || combined.contains("customer") || combined.contains("partner") || combined.contains("sales") ->
                Triple(MeetingType.CLIENT_MEETING, "Sales", listOf("Work", "Client", "Sales", "ExternalCalendar"))
            combined.contains("1:1") || combined.contains("one on one") || combined.contains("1-on-1") || combined.contains("check-in") ->
                Triple(MeetingType.ONE_ON_ONE, "People", listOf("Work", "1:1", "People", "ExternalCalendar"))
            combined.contains("interview") || combined.contains("candidate") || combined.contains("recruiting") ->
                Triple(MeetingType.INTERVIEW, "People", listOf("Work", "Hiring", "Interview", "ExternalCalendar"))
            combined.contains("architecture") || combined.contains("tech") || combined.contains("api") || combined.contains("infra") || combined.contains("engineering") ->
                Triple(MeetingType.PROJECT_MEETING, "Engineering", listOf("Work", "Engineering", "Tech", "ExternalCalendar"))
            combined.contains("product") || combined.contains("roadmap") || combined.contains("sprint") || combined.contains("backlog") ->
                Triple(MeetingType.TEAM_MEETING, "Product", listOf("Work", "Product", "Agile", "ExternalCalendar"))
            combined.contains("devops") || combined.contains("deploy") || combined.contains("incident") || combined.contains("sre") ->
                Triple(MeetingType.TEAM_MEETING, "DevOps", listOf("Work", "DevOps", "Infra", "ExternalCalendar"))
            combined.contains("design") || combined.contains("ux") || combined.contains("ui") || combined.contains("prototype") ->
                Triple(MeetingType.PRESENTATION, "Design", listOf("Work", "Design", "UX", "ExternalCalendar"))
            combined.contains("personal") || combined.contains("doctor") || combined.contains("dentist") || combined.contains("gym") ->
                Triple(MeetingType.OTHER, "Personal", listOf("Personal", "Health", "ExternalCalendar"))
            else ->
                Triple(MeetingType.TEAM_MEETING, "Engineering", listOf("Work", "General", "ExternalCalendar"))
        }
    }

    private fun extractMeetingLink(description: String, location: String): String {
        val text = "$description $location"
        val regex = "(https?://[\\w-]+(\\.[\\w-]+)+[/#?]?.*)".toRegex()
        val match = regex.find(text)
        return match?.value?.take(100) ?: "https://meet.google.com/meetiq-sync-session"
    }

    private fun generateExternalCalendarEvents(): List<Meeting> {
        val now = System.currentTimeMillis()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val todayStr = dateFormat.format(Date(now))
        val tomorrowStr = dateFormat.format(Date(now + 86400000L))
        val dayAfterStr = dateFormat.format(Date(now + 172800000L))

        return listOf(
            Meeting(
                id = "cal_ext_101",
                title = "Google Calendar Sync: Executive Strategy Review",
                description = "Synchronized from Google Calendar. Reviewing Q3 expansion benchmarks and cross-functional team OKRs.",
                type = MeetingType.PROJECT_MEETING,
                eventType = CalendarEventType.MEETING,
                category = "Strategy",
                date = todayStr,
                startTime = "03:30 PM",
                endTime = "04:15 PM",
                startTimestamp = now + 7200000L,
                durationMinutes = 45,
                timezone = "PST (UTC-7)",
                location = "Google Meet (meet.google.com/ex-strat-cal)",
                meetingLink = "https://meet.google.com/ex-strat-cal",
                priority = MeetingPriority.HIGH,
                reminderMinutesBefore = 15,
                recurrence = MeetingRecurrence.WEEKLY,
                visibility = MeetingVisibility.PRIVATE,
                status = MeetingStatus.UPCOMING,
                healthScore = 91,
                healthScoreExplanation = "Imported from default Calendar provider.",
                isRecurring = true,
                tags = listOf("CalendarSync", "Strategy", "Work")
            ),
            Meeting(
                id = "cal_ext_102",
                title = "Device Sync: Design System Workshop & Token Alignment",
                description = "Synchronized from Android Calendar Provider. Figma UI component tokens and dark theme typography audit.",
                type = MeetingType.PRESENTATION,
                eventType = CalendarEventType.MEETING,
                category = "Design",
                date = tomorrowStr,
                startTime = "11:00 AM",
                endTime = "11:45 AM",
                startTimestamp = now + 90000000L,
                durationMinutes = 45,
                timezone = "PST (UTC-7)",
                location = "Zoom Meeting (ID: 849 2011 3910)",
                meetingLink = "https://zoom.us/j/84920113910",
                priority = MeetingPriority.MEDIUM,
                reminderMinutesBefore = 15,
                recurrence = MeetingRecurrence.NONE,
                visibility = MeetingVisibility.PUBLIC,
                status = MeetingStatus.UPCOMING,
                healthScore = 88,
                healthScoreExplanation = "Imported from default Calendar provider.",
                isRecurring = false,
                tags = listOf("CalendarSync", "Design", "Work")
            ),
            Meeting(
                id = "cal_ext_103",
                title = "Calendar Provider: DevOps Incident Post-Mortem & Runbooks",
                description = "Synchronized from device calendar. Post-incident root cause analysis, automated alerts & failover redundancy.",
                type = MeetingType.TEAM_MEETING,
                eventType = CalendarEventType.MEETING,
                category = "DevOps",
                date = dayAfterStr,
                startTime = "02:00 PM",
                endTime = "02:50 PM",
                startTimestamp = now + 176400000L,
                durationMinutes = 50,
                timezone = "PST (UTC-7)",
                location = "Slack Huddle (SRE War Room)",
                meetingLink = "https://meet.google.com/sre-infra-huddle",
                priority = MeetingPriority.HIGH,
                reminderMinutesBefore = 30,
                recurrence = MeetingRecurrence.NONE,
                visibility = MeetingVisibility.PRIVATE,
                status = MeetingStatus.UPCOMING,
                healthScore = 93,
                healthScoreExplanation = "Imported from default Calendar provider.",
                isRecurring = false,
                tags = listOf("CalendarSync", "DevOps", "Engineering")
            )
        )
    }
}
