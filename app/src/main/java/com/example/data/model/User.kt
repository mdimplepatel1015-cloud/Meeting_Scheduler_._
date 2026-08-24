package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class UserRole {
    ADMIN,
    MANAGER,
    EMPLOYEE,
    GUEST
}

@Entity(tableName = "users")
data class User(
    @PrimaryKey val id: String = "usr_current",
    val name: String = "Alex Morgan",
    val email: String = "alex.morgan@enterprise.ai",
    val role: UserRole = UserRole.MANAGER,
    val avatarInitials: String = "AM",
    val avatarColorHex: String = "#4F46E5",
    val timezone: String = "America/Los_Angeles (PST)",
    val workingHoursStart: String = "09:00",
    val workingHoursEnd: String = "17:00",
    val preferredDurationMinutes: Int = 30,
    val preferredDays: String = "Mon, Tue, Wed, Thu, Fri",
    val emailNotifications: Boolean = true,
    val pushNotifications: Boolean = true,
    val aiMeetingAnalysis: Boolean = true,
    val autoSummarize: Boolean = true,
    val language: String = "English (US)"
)
