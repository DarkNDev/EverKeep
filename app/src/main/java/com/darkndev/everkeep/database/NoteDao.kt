package com.darkndev.everkeep.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.darkndev.everkeep.models.Note
import kotlinx.coroutines.flow.Flow
import java.time.ZonedDateTime

@Dao
interface NoteDao {

    @Insert
    suspend fun insertItem(note: Note): Long

    @Query("UPDATE note_table SET title=:titleText, modified=:time WHERE id=:userId")
    suspend fun updateTitle(
        userId: Long,
        titleText: String,
        time: Long = System.currentTimeMillis(),
    )

    @Query("UPDATE note_table SET content=:contentText, modified=:time WHERE id=:userId")
    suspend fun updateContent(
        userId: Long,
        contentText: String,
        time: Long = System.currentTimeMillis(),
    )

    @Query("UPDATE note_table SET imageArray=:array, modified=:time WHERE id=:userId")
    suspend fun updateScribble(
        userId: Long,
        array: ByteArray,
        time: Long = System.currentTimeMillis(),
    )

    @Query("UPDATE note_table SET priority=:priority WHERE id=:userId")
    suspend fun updatePriority(
        userId: Long,
        priority: Int
    )

    @Query("UPDATE note_table SET label=:labelText WHERE id=:userId")
    suspend fun updateLabel(
        userId: Long,
        labelText: String
    )

    @Query("UPDATE note_table SET reminderActive=:reminderActive, reminderTime=:reminderTime,reminderRepeat=:reminderRepeat WHERE id=:userId")
    suspend fun updateReminder(
        userId: Long,
        reminderActive: Boolean,
        reminderTime: ZonedDateTime?,
        reminderRepeat: Long = 0L
    )

    @Query("UPDATE note_table SET pinned=:pinned, archived=:archived, deleted=:deleted WHERE id=:userId")
    suspend fun updatePinned(
        userId: Long,
        pinned: Boolean,
        archived: Boolean = false,
        deleted: Boolean = false
    )

    @Query("UPDATE note_table SET archived=:archived, pinned=:pinned, deleted=:deleted WHERE id in (:userIds)")
    suspend fun updateArchivedAll(
        userIds: LongArray,
        archived: Boolean,
        pinned: Boolean = false,
        deleted: Boolean = false
    )

    @Query("UPDATE note_table SET deleted=:deleted, pinned=:pinned, archived=:archived WHERE id in (:userIds)")
    suspend fun updateRecycleAll(
        userIds: LongArray,
        deleted: Boolean,
        pinned: Boolean = false,
        archived: Boolean = false
    )

    @Query("DELETE FROM note_table WHERE id in (:userIds)")
    suspend fun deleteItems(userIds: LongArray)

    @Query("SELECT * FROM note_table WHERE id=:userId")
    suspend fun getItem(userId: Long): Note?

    @Query("SELECT * FROM note_table WHERE id=:userId")
    fun getItemFlow(userId: Long): Flow<Note>

    @Query("SELECT * FROM note_table WHERE title LIKE '%' || :text || '%' AND deleted=:deleted ORDER BY modified ASC")
    fun getQueryItems(text: String, deleted: Boolean = false): Flow<List<Note>>

    @Query("SELECT * FROM note_table WHERE deleted=:deleted ORDER BY modified ASC")
    fun getItems(deleted: Boolean = false): Flow<List<Note>>
}