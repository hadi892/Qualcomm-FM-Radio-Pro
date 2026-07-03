package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.RecordingRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface RecordingRecordDao {
    @Query("SELECT * FROM recording_records ORDER BY timestamp DESC")
    fun getAllRecordings(): Flow<List<RecordingRecord>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecording(recording: RecordingRecord)

    @Query("DELETE FROM recording_records WHERE id = :id")
    suspend fun deleteRecordingById(id: Int)
}
