package com.example.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.local.entity.PrescriptionSummaryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PrescriptionSummaryDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSummary(summary: PrescriptionSummaryEntity)

    @Query("SELECT * FROM prescription_summaries ORDER BY timestamp DESC")
    fun getAllSummaries(): Flow<List<PrescriptionSummaryEntity>>

    @Query("SELECT * FROM prescription_summaries ORDER BY timestamp DESC")
    suspend fun getAllSummariesList(): List<PrescriptionSummaryEntity>

    @Query("SELECT * FROM prescription_summaries WHERE id = :id")
    suspend fun getSummaryById(id: String): PrescriptionSummaryEntity?

    @Query("DELETE FROM prescription_summaries WHERE id = :id")
    suspend fun deleteSummaryById(id: String)

    @Query("DELETE FROM prescription_summaries")
    suspend fun deleteAllSummaries()
}
