package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "station_presets")
data class StationPreset(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val frequency: Float,
    val stationName: String,
    val programType: String = "Pop / Rock",
    val regionLabel: String = "Europe",
    val isFavorite: Boolean = true,
    val rdsText: String = "",
    val timestamp: Long = System.currentTimeMillis()
)
