package com.toprunner.imagestory.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "musics")
data class MusicEntity(
    @PrimaryKey(autoGenerate = true)
    val music_id: Long = 0,
    val title: String,
    val music_path: String,
    val attribute: String, // 장르 정보
    val created_at: Long = System.currentTimeMillis()
)