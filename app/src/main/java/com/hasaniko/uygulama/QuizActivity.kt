package com.hasaniko.uygulama

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*
import kotlin.random.Random

class QuizActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private var quizList: List<Word> = listOf()
    private var currentIndex = 0
    private var correctCount = 0
    private var wrongWordsList = mutableListOf<Word>()

    private lateinit var progressBar: ProgressBar
    private lateinit var tvWord: TextView
    private lateinit var tvProgressInfo: TextView
    private lateinit var buttons: List<Button>
    private var isEnglishQuestion = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)

        db = AppDatabase.getInstance(this)

        val level = intent.getIntExtra("level", 0)
        val limit = intent.getIntExtra("limit", 10)
        val activeLevel = intent.getIntExtra("activeLevel", 1)

        setupViews()
        loadQuizData(level, limit, activeLevel)
    }

    private fun setupViews() {
        progressBar = findViewById(R.id.quizProgressBar)
        tvWord = findViewById(R.id.tvQuizWord)
        tvProgressInfo = findViewById(R.id.tvQuizProgressInfo)
        buttons = listOf(
            findViewById(R.id.btnOpt1), findViewById(R.id.btnOpt2),
            findViewById(R.id.btnOpt3), findViewById(R.id.btnOpt4)
        )
    }

    private fun loadQuizData(level: Int, limit: Int, activeLevel: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            quizList = when (level) {
                -1 -> db.wordDao().getWrongWords().shuffled().take(limit)
                0 -> db.wordDao().getMixedQuizWordsUpToLevel(activeLevel, limit)
                else -> db.wordDao().getQuizWordsByLevel(level, limit)
            }

            withContext(Dispatchers.Main) {
                if (quizList.isNotEmpty()) {
                    progressBar.max = quizList.size
                    showQuestion()
                } else {
                    Toast.makeText(this@QuizActivity, "Seçili listede kelime bulunamadı!", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
        }
    }

    private fun showQuestion() {
        if (currentIndex >= quizList.size) {
            showFinalResult()
            return
        }

        val currentWordObj = quizList[currentIndex]

        progressBar.progress = currentIndex + 1
        tvProgressInfo.text = "${currentIndex + 1} / ${quizList.size}"

        isEnglishQuestion = Random.nextBoolean()
        tvWord.text = if (isEnglishQuestion) currentWordObj.word else currentWordObj.meaning

        CoroutineScope(Dispatchers.IO).launch {
            val options = db.wordDao().getRandomQuizWords(4).toMutableList()
            if (!options.any { it.id == currentWordObj.id }) {
                options[0] = currentWordObj
            }
            val shuffledOptions = options.shuffled()

            withContext(Dispatchers.Main) {
                for (i in buttons.indices) {
                    if (i < shuffledOptions.size) {
                        buttons[i].visibility = View.VISIBLE
                        buttons[i].text = if (isEnglishQuestion) shuffledOptions[i].meaning else shuffledOptions[i].word
                        buttons[i].setBackgroundColor(Color.WHITE)
                        buttons[i].setTextColor(Color.parseColor("#102A43"))
                        buttons[i].isEnabled = true
                        buttons[i].setOnClickListener { checkAnswer(shuffledOptions[i], buttons[i]) }
                    } else {
                        buttons[i].visibility = View.INVISIBLE
                    }
                }
            }
        }
    }

    private fun checkAnswer(selected: Word, clickedBtn: Button) {
        buttons.forEach { it.isEnabled = false }
        val correct = quizList[currentIndex]

        if (selected.id == correct.id) {
            clickedBtn.setBackgroundColor(Color.parseColor("#4CAF50"))
            clickedBtn.setTextColor(Color.WHITE)
            correctCount++

            if (correct.wrongAnswerCount > 0) {
                CoroutineScope(Dispatchers.IO).launch {
                    correct.wrongAnswerCount = 0
                    db.wordDao().updateWord(correct)
                }
            }
        } else {
            clickedBtn.setBackgroundColor(Color.parseColor("#F44336"))
            clickedBtn.setTextColor(Color.WHITE)
            wrongWordsList.add(correct)
            updateErrorInDb(correct)
        }

        Handler(Looper.getMainLooper()).postDelayed({
            currentIndex++
            showQuestion()
        }, 700)
    }

    private fun updateErrorInDb(word: Word) {
        CoroutineScope(Dispatchers.IO).launch {
            word.wrongAnswerCount++
            db.wordDao().updateWord(word)
        }
    }

    private fun showFinalResult() {
        val analysis = wrongWordsList.groupBy { it.type }
            .map { "${it.key}: ${it.value.size} hata" }
            .joinToString("\n")

        val resultSummary = "Doğru Sayısı: $correctCount / ${quizList.size}"
        val analysisMsg = if (analysis.isEmpty()) "Hatanız yok!" else "Eksik olduğunuz türler:\n$analysis"

        AlertDialog.Builder(this)
            .setTitle("Quiz Tamamlandı!")
            .setMessage("$resultSummary\n\n$analysisMsg")
            .setCancelable(false)
            .setPositiveButton("Ana Menüye Dön") { _, _ ->
                finish()
            }
            .show()
    }
}