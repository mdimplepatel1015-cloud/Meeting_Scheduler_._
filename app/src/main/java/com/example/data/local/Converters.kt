package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.ActionItemStatus
import com.example.data.model.CalendarEventType
import com.example.data.model.MeetingPriority
import com.example.data.model.MeetingRecurrence
import com.example.data.model.MeetingStatus
import com.example.data.model.MeetingType
import com.example.data.model.MeetingVisibility
import com.example.data.model.NotificationType
import com.example.data.model.RsvpStatus
import com.example.data.model.UserRole

class Converters {
    @TypeConverter
    fun fromUserRole(role: UserRole): String = role.name

    @TypeConverter
    fun toUserRole(value: String): UserRole = try {
        UserRole.valueOf(value)
    } catch (e: Exception) {
        UserRole.EMPLOYEE
    }

    @TypeConverter
    fun fromMeetingType(type: MeetingType): String = type.name

    @TypeConverter
    fun toMeetingType(value: String): MeetingType = try {
        MeetingType.valueOf(value)
    } catch (e: Exception) {
        MeetingType.TEAM_MEETING
    }

    @TypeConverter
    fun fromCalendarEventType(type: CalendarEventType): String = type.name

    @TypeConverter
    fun toCalendarEventType(value: String): CalendarEventType = try {
        CalendarEventType.valueOf(value)
    } catch (e: Exception) {
        CalendarEventType.MEETING
    }

    @TypeConverter
    fun fromMeetingPriority(priority: MeetingPriority): String = priority.name

    @TypeConverter
    fun toMeetingPriority(value: String): MeetingPriority = try {
        MeetingPriority.valueOf(value)
    } catch (e: Exception) {
        MeetingPriority.MEDIUM
    }

    @TypeConverter
    fun fromMeetingRecurrence(recurrence: MeetingRecurrence): String = recurrence.name

    @TypeConverter
    fun toMeetingRecurrence(value: String): MeetingRecurrence = try {
        MeetingRecurrence.valueOf(value)
    } catch (e: Exception) {
        MeetingRecurrence.NONE
    }

    @TypeConverter
    fun fromMeetingVisibility(visibility: MeetingVisibility): String = visibility.name

    @TypeConverter
    fun toMeetingVisibility(value: String): MeetingVisibility = try {
        MeetingVisibility.valueOf(value)
    } catch (e: Exception) {
        MeetingVisibility.PRIVATE
    }

    @TypeConverter
    fun fromMeetingStatus(status: MeetingStatus): String = status.name

    @TypeConverter
    fun toMeetingStatus(value: String): MeetingStatus = try {
        MeetingStatus.valueOf(value)
    } catch (e: Exception) {
        MeetingStatus.UPCOMING
    }

    @TypeConverter
    fun fromRsvpStatus(status: RsvpStatus): String = status.name

    @TypeConverter
    fun toRsvpStatus(value: String): RsvpStatus = try {
        RsvpStatus.valueOf(value)
    } catch (e: Exception) {
        RsvpStatus.PENDING
    }

    @TypeConverter
    fun fromActionItemStatus(status: ActionItemStatus): String = status.name

    @TypeConverter
    fun toActionItemStatus(value: String): ActionItemStatus = try {
        ActionItemStatus.valueOf(value)
    } catch (e: Exception) {
        ActionItemStatus.NOT_STARTED
    }

    @TypeConverter
    fun fromNotificationType(type: NotificationType): String = type.name

    @TypeConverter
    fun toNotificationType(value: String): NotificationType = try {
        NotificationType.valueOf(value)
    } catch (e: Exception) {
        NotificationType.INVITATION
    }

    @TypeConverter
    fun fromStringList(list: List<String>?): String = list?.joinToString(",") ?: ""

    @TypeConverter
    fun toStringList(value: String?): List<String> =
        if (value.isNullOrBlank()) emptyList()
        else value.split(",").map { it.trim() }.filter { it.isNotEmpty() }
}
