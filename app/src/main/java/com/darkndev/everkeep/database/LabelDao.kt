package com.darkndev.everkeep.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.darkndev.everkeep.models.Label
import kotlinx.coroutines.flow.Flow

@Dao
interface LabelDao {

    @Insert
    suspend fun insertLabel(label: Label)

    @Delete
    suspend fun deleteLabel(label: Label)

    @Query("SELECT * FROM label_table WHERE id = :labelId")
    suspend fun getLabel(labelId: Long): Label?

    @Query("SELECT * FROM label_table")
    fun getAllFlowLabels(): Flow<List<Label>>
}