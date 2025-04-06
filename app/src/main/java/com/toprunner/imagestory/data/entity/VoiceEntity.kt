package com.toprunner.imagestory.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "voices")
data class VoiceEntity(
    @PrimaryKey(autoGenerate = true)
    val voice_id: Long = 0,
    val title: String,
    val voice_path: String,
    val attribute: String, // 음성 특성 파일 경로
    val created_at: Long = System.currentTimeMillis()
)