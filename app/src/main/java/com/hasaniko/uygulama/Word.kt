package com.hasaniko.uygulama

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "words")
data class Word(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val word: String,
    val type: String,
    val meaning: String,
    val example_en: String,
    val example_tr: String,
    val level: Int,
    var isLearned: Boolean = false,
    var wrongAnswerCount: Int = 0
)