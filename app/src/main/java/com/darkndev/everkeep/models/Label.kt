package com.darkndev.everkeep.models

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Entity(tableName = "label_table")
@Parcelize
data class Label(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val label: String
) : Parcelable
