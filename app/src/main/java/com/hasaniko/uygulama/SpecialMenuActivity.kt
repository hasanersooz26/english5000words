package com.hasaniko.uygulama

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity

class SpecialMenuActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_special_menu)

        findViewById<Button>(R.id.btnSpecialLearn).setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra("level", 0)
            intent.putExtra("limit", 20)
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnReading).setOnClickListener {
            showQuestionCountDialog("Reading")
        }

        findViewById<Button>(R.id.btnVocabulary).setOnClickListener {
            showQuestionCountDialog("Vocabulary")
        }

        findViewById<Button>(R.id.btnGrammar).setOnClickListener {
            showQuestionCountDialog("Grammar")
        }

        findViewById<Button>(R.id.btnSpecialMistakes).setOnClickListener {
            val intent = Intent(this, SpecialQuizActivity::class.java)
            intent.putExtra("category", "Hatalar")
            intent.putExtra("limit", 50)
            startActivity(intent)
        }
    }

    private fun showQuestionCountDialog(category: String) {
        val options = arrayOf("10 Soru", "20 Soru", "30 Soru", "50 Soru", "Tüm Sorular")
        val limits = intArrayOf(10, 20, 30, 50, Int.MAX_VALUE)

        AlertDialog.Builder(this)
            .setTitle("$category - Soru Sayısı Seçin")
            .setItems(options) { _, which ->
                val selectedLimit = limits[which]
                startSpecialQuiz(category, selectedLimit)
            }.show()
    }

    private fun startSpecialQuiz(category: String, limit: Int) {
        val intent = Intent(this, SpecialQuizActivity::class.java)
        intent.putExtra("category", category)
        intent.putExtra("limit", limit)
        startActivity(intent)
    }
}