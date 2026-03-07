package com.hasaniko.uygulama

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface WordDao {
    @Insert
    fun insertAll(words: List<Word>)

    @Query("SELECT * FROM words WHERE isLearned = 0 AND word != '' AND meaning != '' ORDER BY id ASC LIMIT 1")
    fun getNextResumeWord(): Word?

    @Query("SELECT MIN(level) FROM words WHERE isLearned = 0 AND word != '' AND meaning != ''")
    fun getCurrentActiveLevel(): Int?

    @Query("SELECT MAX(level) FROM words")
    fun getMaxLevel(): Int

    @Query("SELECT * FROM words WHERE level = :level AND isLearned = 0 AND word != '' AND meaning != '' ORDER BY id ASC")
    fun getUnlearnedWordsByLevel(level: Int): List<Word>

    @Query("SELECT * FROM words WHERE level = :level AND word != '' AND meaning != '' ORDER BY id ASC")
    fun getAllWordsByLevel(level: Int): List<Word>

    @Query("SELECT * FROM words WHERE level = :level AND word != '' AND meaning != '' ORDER BY RANDOM() LIMIT :limit")
    fun getQuizWordsByLevel(level: Int, limit: Int): List<Word>

    @Query("SELECT * FROM words WHERE level <= :maxLevel AND word != '' AND meaning != '' ORDER BY RANDOM() LIMIT :limit")
    fun getMixedQuizWordsUpToLevel(maxLevel: Int, limit: Int): List<Word>

    @Query("SELECT * FROM words WHERE isLearned = 1 AND word != '' AND meaning != '' ORDER BY RANDOM() LIMIT :limit")
    fun getLearnedQuizWords(limit: Int): List<Word>

    @Query("SELECT * FROM words WHERE word != '' AND meaning != '' ORDER BY RANDOM() LIMIT :limit")
    fun getRandomQuizWords(limit: Int): List<Word>

    @Query("SELECT COUNT(*) FROM words WHERE isLearned = 1 AND word != '' AND meaning != ''")
    fun getTotalLearnedCount(): Int

    @Query("SELECT COUNT(*) FROM words WHERE isLearned = 1 AND level = :currentLevel AND word != '' AND meaning != ''")
    fun getLearnedCount(currentLevel: Int): Int

    @Query("SELECT COUNT(*) FROM words WHERE level = :currentLevel AND word != '' AND meaning != ''")
    fun getTotalCountInLevel(currentLevel: Int): Int

    @Query("SELECT COUNT(*) FROM words WHERE wrongAnswerCount > 0 AND word != '' AND meaning != ''")
    fun getTotalErrorCount(): Int

    @Query("SELECT * FROM words WHERE wrongAnswerCount > 0 AND word != '' AND meaning != '' ORDER BY wrongAnswerCount DESC")
    fun getWrongWords(): List<Word>

    @Update
    fun updateWord(word: Word)
}