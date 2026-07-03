package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.RecordingRecordDao
import com.example.data.dao.StationPresetDao
import com.example.data.entity.RecordingRecord
import com.example.data.entity.StationPreset

@Database(
    entities = [StationPreset::class, RecordingRecord::class],
    version = 1,
    exportSchema = false
)
abstract class FmDatabase : RoomDatabase() {
    abstract fun stationPresetDao(): StationPresetDao
    abstract fun recordingRecordDao(): RecordingRecordDao

    companion object {
        @Volatile
        private var INSTANCE: FmDatabase? = null

        fun getDatabase(context: Context): FmDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FmDatabase::class.java,
                    "qualcomm_fm_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
