package com.toprunner.imagestory.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.toprunner.imagestory.data.dao.*
import com.toprunner.imagestory.data.entity.*

@Database(
    entities = [
        FairyTaleEntity::class,
        VoiceEntity::class,
        ImageEntity::class,
        TextEntity::class,
        MusicEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun fairyTaleDao(): FairyTaleDao
    abstract fun voiceDao(): VoiceDao
    abstract fun imageDao(): ImageDao
    abstract fun textDao(): TextDao
    abstract fun musicDao(): MusicDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "fairy_tale_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}