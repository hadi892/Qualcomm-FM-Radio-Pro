package com.example.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data.entity.StationPreset
import kotlinx.coroutines.flow.Flow

@Dao
interface StationPresetDao {
    @Query("SELECT * FROM station_presets ORDER BY frequency ASC")
    fun getAllPresets(): Flow<List<StationPreset>>

    @Query("SELECT * FROM station_presets WHERE isFavorite = 1 ORDER BY frequency ASC")
    fun getFavorites(): Flow<List<StationPreset>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreset(preset: StationPreset)

    @Query("DELETE FROM station_presets WHERE id = :id")
    suspend fun deletePresetById(id: Int)

    @Query("DELETE FROM station_presets WHERE frequency = :freq")
    suspend fun deletePresetByFrequency(freq: Float)
}
