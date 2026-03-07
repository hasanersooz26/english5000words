package com.hasaniko.uygulama

import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONArray
import java.io.InputStream

class SpecialQuizActivity : AppCompatActivity() {

    private lateinit var tvCategory: TextView
    private lateinit var tvProgress: TextView
    private lateinit var tvQuestionText: TextView
    private lateinit var buttons: List<Button>

    private var questionList: List<SpecialQuestion> = listOf()
    private var currentIndex = 0
    private var correctCount = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_special_quiz)

        tvCategory = findViewById(R.id.tvCategory)
        tvProgress = findViewById(R.id.tvProgress)
        tvQuestionText = findViewById(R.id.tvQuestionText)
        buttons = listOf(
            findViewById(R.id.btnOptionA),
            findViewById(R.id.btnOptionB),
            findViewById(R.id.btnOptionC),
            findViewById(R.id.btnOptionD)
        )

        val category = intent.getStringExtra("category") ?: "Vocabulary"
        val limit = intent.getIntExtra("limit", 10)

        tvCategory.text = if (category == "Hatalar") "Hatalı Sorular" else category

        loadQuestionsFromJson(category, limit)

        if (questionList.isNotEmpty()) {
            showQuestion()
        } else {
            Toast.makeText(this, "Çözülecek soru bulunamadı! (Hatanız yok veya kategori boş)", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    private fun loadQuestionsFromJson(category: String, limit: Int) {
        val jsonString: String
        try {
            val inputStream: InputStream = assets.open("b1kelimeler.json")
            val size = inputStream.available()
            val buffer = ByteArray(size)
            inputStream.read(buffer)
            inputStream.close()
            jsonString = String(buffer, Charsets.UTF_8)

            val jsonArray = JSONArray(jsonString)
            val allQuestions = mutableListOf<SpecialQuestion>()

            val prefs = getSharedPreferences("SpecialQuizPrefs", Context.MODE_PRIVATE)
            val wrongQuestionsSet = prefs.getStringSet("wrong_questions", setOf()) ?: setOf()

            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val qText = jsonObject.getString("questionText")
                val qCategory = jsonObject.getString("category")

                val isTargetCategory = qCategory == category
                val isErrorMode = category == "Hatalar" && wrongQuestionsSet.contains(qText)

                if (isTargetCategory || isErrorMode) {
                    allQuestions.add(
                        SpecialQuestion(
                            qCategory,
                            qText,
                            jsonObject.getString("optionA"),
                            jsonObject.getString("optionB"),
                            jsonObject.getString("optionC"),
                            jsonObject.getString("optionD"),
                            jsonObject.getString("correctAnswer")
                        )
                    )
                }
            }

            questionList = allQuestions.shuffled().take(limit)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun showQuestion() {
        if (currentIndex >= questionList.size) {
            showResult()
            return
        }

        val q = questionList[currentIndex]
        tvProgress.text = "${currentIndex + 1} / ${questionList.size}"
        tvQuestionText.text = q.questionText

        val options = listOf(q.optionA, q.optionB, q.optionC, q.optionD).shuffled()

        for (i in buttons.indices) {
            buttons[i].text = options[i]
            buttons[i].setBackgroundColor(Color.WHITE)
            buttons[i].setTextColor(Color.parseColor("#102A43"))
            buttons[i].isEnabled = true
            buttons[i].setOnClickListener { checkAnswer(options[i], buttons[i], q) }
        }
    }

    private fun checkAnswer(selected: String, clickedBtn: Button, q: SpecialQuestion) {
        buttons.forEach { it.isEnabled = false }

        val prefs = getSharedPreferences("SpecialQuizPrefs", Context.MODE_PRIVATE)
        val wrongs = prefs.getStringSet("wrong_questions", mutableSetOf())?.toMutableSet() ?: mutableSetOf()

        if (selected == q.correctAnswer) {
            clickedBtn.setBackgroundColor(Color.parseColor("#4CAF50"))
            clickedBtn.setTextColor(Color.WHITE)
            correctCount++

            if (wrongs.contains(q.questionText)) {
                wrongs.remove(q.questionText)
                prefs.edit().putStringSet("wrong_questions", wrongs).apply()
            }

        } else {
            clickedBtn.setBackgroundColor(Color.parseColor("#F44336"))
            clickedBtn.setTextColor(Color.WHITE)

            buttons.find { it.text == q.correctAnswer }?.setBackgroundColor(Color.parseColor("#4CAF50"))
            buttons.find { it.text == q.correctAnswer }?.setTextColor(Color.WHITE)

            wrongs.add(q.questionText)
            prefs.edit().putStringSet("wrong_questions", wrongs).apply()
        }

        Handler(Looper.getMainLooper()).postDelayed({
            currentIndex++
            showQuestion()
        }, 1000)
    }

    private fun showResult() {
        AlertDialog.Builder(this)
            .setTitle("Tebrikler!")
            .setMessage("Skorun: $correctCount / ${questionList.size}")
            .setCancelable(false)
            .setPositiveButton("Menüye Dön") { _, _ -> finish() }
            .show()
    }
}