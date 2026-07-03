package com.example.data.repository

import com.example.data.dao.RecordingRecordDao
import com.example.data.dao.StationPresetDao
import com.example.data.entity.RecordingRecord
import com.example.data.entity.StationPreset
import kotlinx.coroutines.flow.Flow

class FmRepository(
    private val presetDao: StationPresetDao,
    private val recordingDao: RecordingRecordDao
) {
    val allPresets: Flow<List<StationPreset>> = presetDao.getAllPresets()
    val favorites: Flow<List<StationPreset>> = presetDao.getFavorites()
    val allRecordings: Flow<List<RecordingRecord>> = recordingDao.getAllRecordings()

    suspend fun insertPreset(preset: StationPreset) = presetDao.insertPreset(preset)
    suspend fun deletePresetById(id: Int) = presetDao.deletePresetById(id)
    suspend fun deletePresetByFrequency(freq: Float) = presetDao.deletePresetByFrequency(freq)

    suspend fun insertRecording(recording: RecordingRecord) = recordingDao.insertRecording(recording)
    suspend fun deleteRecordingById(id: Int) = recordingDao.deleteRecordingById(id)

    suspend fun populateDefaultsIfEmpty(currentList: List<StationPreset>) {
        if (currentList.isEmpty()) {
            val defaults = listOf(
                StationPreset(frequency = 88.5f, stationName = "BBC Radio 2", programType = "Pop / Variety", isFavorite = true, rdsText = "Now Playing: Golden Hits"),
                StationPreset(frequency = 94.1f, stationName = "K-Rock Snapdragon", programType = "Alternative Rock", isFavorite = true, rdsText = "Snapdragon Sound High-Res Audio Live"),
                StationPreset(frequency = 98.3f, stationName = "Classical Symphony", programType = "Classical Music", isFavorite = false, rdsText = "Beethoven Symphony No. 9"),
                StationPreset(frequency = 104.2f, stationName = "QCOM Beats 5G", programType = "Electronic / Dance", isFavorite = true, rdsText = "Qualcomm Hexagon DSP Immersive Audio"),
                StationPreset(frequency = 107.5f, stationName = "Smooth Jazz FM", programType = "Jazz", isFavorite = true, rdsText = "Late Night Smooth Jazz & Lounge")
            )
            defaults.forEach { insertPreset(it) }
        }
    }
}
