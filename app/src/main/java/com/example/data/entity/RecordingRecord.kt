package com.example.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recording_records")
data class RecordingRecord(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val frequency: Float,
    val stationName: String,
    val filePath: String,
    val durationSeconds: Int,
    val fileSizeKb: Int,
    val timestamp: Long = System.currentTimeMillis()
)
