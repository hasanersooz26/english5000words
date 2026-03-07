package com.hasaniko.uygulama

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface WordDao {
    @Insert
    fun insertAll(words: List<Word>)

    @Query("SELECT * FROM words WHERE isLearned = 0 ORDER BY id ASC LIMIT 1")
    fun getNextResumeWord(): Word?

    @Query("SELECT MAX(level) FROM words WHERE isLearned = 1")
    fun getMaxFinishedLevel(): Int?

    @Query("SELECT * FROM words WHERE isLearned = 1 ORDER BY RANDOM() LIMIT :limit")
    fun getLearnedQuizWords(limit: Int): List<Word>

    @Query("SELECT * FROM words WHERE level = :level AND isLearned = 0 ORDER BY id ASC")
    fun getUnlearnedWordsByLevel(level: Int): List<Word>

    @Query("SELECT * FROM words WHERE level = :level ORDER BY id ASC")
    fun getAllWordsByLevel(level: Int): List<Word>

    @Query("SELECT * FROM words WHERE level = :level ORDER BY RANDOM() LIMIT :limit")
    fun getQuizWordsByLevel(level: Int, limit: Int): List<Word>

    @Query("SELECT * FROM words ORDER BY RANDOM() LIMIT :limit")
    fun getRandomQuizWords(limit: Int): List<Word>

    @Query("SELECT COUNT(*) FROM words WHERE isLearned = 1")
    fun getTotalLearnedCount(): Int

    @Query("SELECT COUNT(*) FROM words WHERE isLearned = 1 AND level = :currentLevel")
    fun getLearnedCount(currentLevel: Int): Int

    @Query("SELECT COUNT(*) FROM words WHERE level = :currentLevel")
    fun getTotalCountInLevel(currentLevel: Int): Int

    @Query("SELECT COUNT(*) FROM words WHERE wrongAnswerCount > 0")
    fun getTotalErrorCount(): Int

    @Query("SELECT * FROM words WHERE wrongAnswerCount > 0 ORDER BY wrongAnswerCount DESC")
    fun getWrongWords(): List<Word>

    @Update
    fun updateWord(word: Word)
}