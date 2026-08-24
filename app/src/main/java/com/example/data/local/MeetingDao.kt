package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ActionItem
import com.example.data.model.ActionItemStatus
import com.example.data.model.AgendaItem
import com.example.data.model.AuditLog
import com.example.data.model.ChatMessage
import com.example.data.model.Decision
import com.example.data.model.Meeting
import com.example.data.model.MeetingNote
import com.example.data.model.MeetingStatus
import com.example.data.model.MeetingSummary
import com.example.data.model.MeetingTemplate
import com.example.data.model.NotificationItem
import com.example.data.model.Participant
import com.example.data.model.TranscriptLine
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingDao {
    // --- Meetings ---
    @Query("SELECT * FROM meetings ORDER BY date ASC, startTime ASC")
    fun getAllMeetings(): Flow<List<Meeting>>

    @Query("SELECT * FROM meetings ORDER BY date ASC, startTime ASC")
    suspend fun getAllMeetingsSync(): List<Meeting>

    @Query("SELECT * FROM meetings WHERE id = :id")
    fun getMeetingById(id: String): Flow<Meeting?>

    @Query("SELECT * FROM meetings WHERE id = :id")
    suspend fun getMeetingByIdSync(id: String): Meeting?

    @Query("SELECT * FROM meetings WHERE date = :date ORDER BY startTime ASC")
    fun getMeetingsByDate(date: String): Flow<List<Meeting>>

    @Query("SELECT * FROM meetings WHERE status = :status ORDER BY date ASC")
    fun getMeetingsByStatus(status: MeetingStatus): Flow<List<Meeting>>

    @Query("SELECT DISTINCT m.* FROM meetings m LEFT JOIN participants p ON m.id = p.meetingId WHERE m.title LIKE '%' || :query || '%' OR p.name LIKE '%' || :query || '%' OR p.email LIKE '%' || :query || '%' ORDER BY m.date ASC, m.startTime ASC")
    fun searchMeetingsByTitleOrParticipant(query: String): Flow<List<Meeting>>

    @Query("SELECT DISTINCT m.* FROM meetings m LEFT JOIN participants p ON m.id = p.meetingId WHERE (:tag = '' OR :tag = 'All' OR m.tags LIKE '%' || :tag || '%') AND (:query = '' OR m.title LIKE '%' || :query || '%' OR p.name LIKE '%' || :query || '%' OR p.email LIKE '%' || :query || '%' OR m.category LIKE '%' || :query || '%') ORDER BY m.date ASC, m.startTime ASC")
    fun searchMeetingsByTagAndQuery(query: String, tag: String): Flow<List<Meeting>>

    @Query("SELECT * FROM meetings WHERE status = 'UPCOMING' ORDER BY date ASC, startTime ASC")
    suspend fun getUpcomingMeetingsListSync(): List<Meeting>

    @Query("SELECT * FROM meetings WHERE tags LIKE '%' || :tag || '%' ORDER BY date ASC, startTime ASC")
    fun getMeetingsByTag(tag: String): Flow<List<Meeting>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: Meeting)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeetings(meetings: List<Meeting>)

    @Update
    suspend fun updateMeeting(meeting: Meeting)

    @Delete
    suspend fun deleteMeeting(meeting: Meeting)

    @Query("DELETE FROM meetings WHERE id = :id")
    suspend fun deleteMeetingById(id: String)

    // --- Participants ---
    @Query("SELECT * FROM participants WHERE meetingId = :meetingId")
    fun getParticipantsForMeeting(meetingId: String): Flow<List<Participant>>

    @Query("SELECT * FROM participants WHERE meetingId = :meetingId")
    suspend fun getParticipantsForMeetingSync(meetingId: String): List<Participant>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipants(participants: List<Participant>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParticipant(participant: Participant)

    @Update
    suspend fun updateParticipant(participant: Participant)

    // --- Agenda Items ---
    @Query("SELECT * FROM agenda_items WHERE meetingId = :meetingId ORDER BY orderIndex ASC")
    fun getAgendaForMeeting(meetingId: String): Flow<List<AgendaItem>>

    @Query("SELECT * FROM agenda_items WHERE meetingId = :meetingId ORDER BY orderIndex ASC")
    suspend fun getAgendaForMeetingSync(meetingId: String): List<AgendaItem>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgendaItems(items: List<AgendaItem>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAgendaItem(item: AgendaItem)

    @Update
    suspend fun updateAgendaItem(item: AgendaItem)

    @Delete
    suspend fun deleteAgendaItem(item: AgendaItem)

    // --- Meeting Notes ---
    @Query("SELECT * FROM meeting_notes WHERE meetingId = :meetingId ORDER BY timestamp ASC")
    fun getNotesForMeeting(meetingId: String): Flow<List<MeetingNote>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: MeetingNote)

    // --- Transcript Lines ---
    @Query("SELECT * FROM transcript_lines WHERE meetingId = :meetingId ORDER BY timestamp ASC")
    fun getTranscriptForMeeting(meetingId: String): Flow<List<TranscriptLine>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranscriptLine(line: TranscriptLine)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTranscriptLines(lines: List<TranscriptLine>)

    // --- Meeting Summaries ---
    @Query("SELECT * FROM meeting_summaries WHERE meetingId = :meetingId LIMIT 1")
    fun getSummaryForMeeting(meetingId: String): Flow<MeetingSummary?>

    @Query("SELECT * FROM meeting_summaries")
    fun getAllMeetingSummaries(): Flow<List<MeetingSummary>>

    @Query("SELECT * FROM meeting_summaries")
    suspend fun getAllMeetingSummariesSync(): List<MeetingSummary>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: MeetingSummary)

    // --- Action Items ---
    @Query("SELECT * FROM action_items ORDER BY createdAt DESC")
    fun getAllActionItems(): Flow<List<ActionItem>>

    @Query("SELECT * FROM action_items WHERE meetingId = :meetingId")
    fun getActionItemsForMeeting(meetingId: String): Flow<List<ActionItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActionItem(item: ActionItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActionItems(items: List<ActionItem>)

    @Update
    suspend fun updateActionItem(item: ActionItem)

    @Query("UPDATE action_items SET status = :newStatus WHERE id = :id")
    suspend fun updateActionItemStatus(id: String, newStatus: ActionItemStatus)

    @Delete
    suspend fun deleteActionItem(item: ActionItem)

    // --- Decisions ---
    @Query("SELECT * FROM decisions ORDER BY createdAt DESC")
    fun getAllDecisions(): Flow<List<Decision>>

    @Query("SELECT * FROM decisions WHERE meetingId = :meetingId")
    fun getDecisionsForMeeting(meetingId: String): Flow<List<Decision>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecision(decision: Decision)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertDecisions(decisions: List<Decision>)

    // --- Notifications ---
    @Query("SELECT * FROM notifications ORDER BY timestamp DESC")
    fun getAllNotifications(): Flow<List<NotificationItem>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotification(notification: NotificationItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNotifications(notifications: List<NotificationItem>)

    @Query("UPDATE notifications SET isRead = 1 WHERE id = :id")
    suspend fun markNotificationAsRead(id: String)

    @Query("UPDATE notifications SET isRead = 1")
    suspend fun markAllNotificationsAsRead()

    // --- Templates ---
    @Query("SELECT * FROM meeting_templates ORDER BY title ASC")
    fun getAllTemplates(): Flow<List<MeetingTemplate>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplates(templates: List<MeetingTemplate>)

    // --- Audit Logs ---
    @Query("SELECT * FROM audit_logs ORDER BY timestamp DESC")
    fun getAllAuditLogs(): Flow<List<AuditLog>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLog(log: AuditLog)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAuditLogs(logs: List<AuditLog>)

    // --- Chat Messages ---
    @Query("SELECT * FROM chat_messages WHERE meetingId = :meetingId ORDER BY id ASC")
    fun getChatMessages(meetingId: String): Flow<List<ChatMessage>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatMessage(message: ChatMessage)
}
