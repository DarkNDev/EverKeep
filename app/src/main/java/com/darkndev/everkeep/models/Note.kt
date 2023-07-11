package com.darkndev.everkeep.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import java.time.ZonedDateTime

@Entity(tableName = "note_table")
@Parcelize
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String = "",
    val content: String = "",
    @Suppress("ArrayInDataClass") @ColumnInfo(typeAffinity = ColumnInfo.BLOB)
    val imageArray: ByteArray = byteArrayOf(),
    val label: String = "Normal",
    val priority: Int = 0,
    val reminderActive: Boolean = false,
    val reminderTime: ZonedDateTime? = null,
    val reminderRepeat: Long = 0L,
    val modified: Long = System.currentTimeMillis(),
    val pinned: Boolean = false,
    val archived: Boolean = false,
    val deleted: Boolean = false
) : Parcelable
