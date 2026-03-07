package com.hasaniko.uygulama

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class LearningActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var wordList: List<Word> = listOf()
    private var currentIndex = 0
    private var mode: String? = null
    private var selectedLevel: Int = 1

    private lateinit var tvWord: TextView
    private lateinit var tvMeaning: TextView
    private lateinit var tvProgress: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_learning)

        db = AppDatabase.getInstance(this)
        mode = intent.getStringExtra("mode")
        selectedLevel = intent.getIntExtra("level", 1)

        setupViews()
        loadWords()

        findViewById<Button>(R.id.btnLearned).setOnClickListener { handleWordAction(true) }
        findViewById<Button>(R.id.btnDontKnow).setOnClickListener { handleWordAction(false) }
    }

    private fun setupViews() {
        tvWord = findViewById(R.id.tvWord)
        tvMeaning = findViewById(R.id.tvMeaning)
        tvProgress = findViewById(R.id.tvProgressInfo)
    }

    private fun loadWords() {
        CoroutineScope(Dispatchers.IO).launch {
            var initialIndex = 0
            wordList = when(mode) {
                "continue" -> {
                    val next = db.wordDao().getNextResumeWord()
                    if (next != null) {
                        val list = db.wordDao().getUnlearnedWordsByLevel(next.level)
                        initialIndex = list.indexOfFirst { it.id == next.id }.coerceAtLeast(0)
                        list
                    } else {
                        listOf()
                    }
                }
                "error_review" -> db.wordDao().getWrongWords()
                else -> {
                    val unlearned = db.wordDao().getUnlearnedWordsByLevel(selectedLevel)
                    if (unlearned.isNotEmpty()) unlearned else db.wordDao().getAllWordsByLevel(selectedLevel)
                }
            }

            withContext(Dispatchers.Main) {
                if (wordList.isNotEmpty()) {
                    currentIndex = initialIndex
                    display()
                } else {
                    Toast.makeText(this@LearningActivity, "Bu listede öğrenecek kelime kalmadı!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun display() {
        val w = wordList[currentIndex]
        tvWord.text = w.word
        tvMeaning.text = w.meaning
        tvProgress.text = "${currentIndex + 1} / ${wordList.size}"
    }

    private fun handleWordAction(isLearned: Boolean) {
        val w = wordList[currentIndex]
        CoroutineScope(Dispatchers.IO).launch {
            if (isLearned) {
                w.isLearned = true
                if (mode == "error_review") w.wrongAnswerCount = 0
            } else {
                w.wrongAnswerCount++
            }
            db.wordDao().updateWord(w)

            withContext(Dispatchers.Main) {
                if (currentIndex < wordList.size - 1) {
                    currentIndex++
                    display()
                } else {
                    Toast.makeText(this@LearningActivity, "Liste Tamamlandı!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }
}