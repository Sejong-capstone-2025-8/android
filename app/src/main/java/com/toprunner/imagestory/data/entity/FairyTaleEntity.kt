package com.toprunner.imagestory.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fairy_tales")
data class FairyTaleEntity(
    @PrimaryKey(autoGenerate = true)
    val fairy_tales_id: Long = 0,
    val title: String,
    val voice_id: Long,
    val image_id: Long,
    val text_id: Long,
    val music_id: Long,
    val attribute: String, // 테마 정보를 JSON 형태로 저장
    val created_at: Long = System.currentTimeMillis()
)