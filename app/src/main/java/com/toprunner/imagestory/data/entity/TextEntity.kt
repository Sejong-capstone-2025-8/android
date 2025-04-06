package com.toprunner.imagestory.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "texts")
data class TextEntity(
    @PrimaryKey(autoGenerate = true)
    val text_id: Long = 0,
    val text_path: String,
    val created_at: Long = System.currentTimeMillis()
)