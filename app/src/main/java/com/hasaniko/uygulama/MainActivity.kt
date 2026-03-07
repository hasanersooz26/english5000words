package com.hasaniko.uygulama

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    private lateinit var db: AppDatabase
    private lateinit var tvCurrentLevel: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var btnErrorReview: Button
    private lateinit var btnSpecial: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getInstance(this)
        DatabaseInitializer.populateDatabase(this, db)

        tvCurrentLevel = findViewById(R.id.tvCurrentLevel)
        progressBar = findViewById(R.id.levelProgressBar)
        btnErrorReview = findViewById(R.id.btnErrorReview)
        btnSpecial = findViewById(R.id.btnSpecial)

        findViewById<Button>(R.id.btnLearn).setOnClickListener {
            showLevelSelectDialog()
        }

        findViewById<Button>(R.id.btnQuiz).setOnClickListener {
            showQuizSetupDialog()
        }

        btnSpecial.setOnClickListener {
            val intent = Intent(this, SpecialMenuActivity::class.java)
            startActivity(intent)
        }

        btnErrorReview.setOnClickListener {
            val options = arrayOf("Hatalı Kelimelere Çalış", "Hatalardan Quiz Çöz")
            AlertDialog.Builder(this)
                .setTitle("Hata Listesi")
                .setItems(options) { _, which ->
                    if (which == 0) {
                        val intent = Intent(this, LearningActivity::class.java)
                        intent.putExtra("mode", "error_review")
                        startActivity(intent)
                    } else {
                        val intent = Intent(this, QuizActivity::class.java)
                        intent.putExtra("level", -1)
                        intent.putExtra("limit", 20)
                        startActivity(intent)
                    }
                }.show()
        }
    }

    private fun showLevelSelectDialog() {
        CoroutineScope(Dispatchers.IO).launch {
            val activeLevel = db.wordDao().getCurrentActiveLevel() ?: db.wordDao().getMaxLevel()

            withContext(Dispatchers.Main) {
                val levelsList = mutableListOf("▶ Kaldığın Yerden Devam Et")
                for (i in 1..activeLevel) {
                    levelsList.add("Bölüm $i")
                }

                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Öğrenme Modu Seçin")
                    .setItems(levelsList.toTypedArray()) { _, which ->
                        val intent = Intent(this@MainActivity, LearningActivity::class.java)
                        if (which == 0) {
                            intent.putExtra("mode", "continue")
                        } else {
                            intent.putExtra("level", which)
                        }
                        startActivity(intent)
                    }.show()
            }
        }
    }

    private fun showQuizSetupDialog() {
        val builder = AlertDialog.Builder(this)
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_quiz_setup, null)
        val spinnerLevel = view.findViewById<Spinner>(R.id.spinnerLevel)
        val etLimit = view.findViewById<EditText>(R.id.etLimit)

        CoroutineScope(Dispatchers.IO).launch {
            val activeLevel = db.wordDao().getCurrentActiveLevel() ?: db.wordDao().getMaxLevel()

            withContext(Dispatchers.Main) {
                val levelsList = mutableListOf("Karışık Sor (Bölüm 1-$activeLevel)")
                for (i in 1..activeLevel) {
                    levelsList.add("Bölüm $i")
                }

                spinnerLevel.adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, levelsList)

                builder.setView(view)
                builder.setPositiveButton("Başlat") { _, _ ->
                    val limitInput = etLimit.text.toString()
                    val limit = if (limitInput.isEmpty()) 10 else limitInput.toInt()

                    val intent = Intent(this@MainActivity, QuizActivity::class.java)
                    intent.putExtra("level", spinnerLevel.selectedItemPosition)
                    intent.putExtra("limit", limit)
                    intent.putExtra("activeLevel", activeLevel)
                    startActivity(intent)
                }
                builder.setNegativeButton("İptal", null)
                builder.show()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun updateUI() {
        CoroutineScope(Dispatchers.IO).launch {
            val learnedCount = db.wordDao().getTotalLearnedCount()
            val totalWords = 150
            val errorCount = db.wordDao().getTotalErrorCount()

            withContext(Dispatchers.Main) {
                tvCurrentLevel.text = "Genel İlerleme: $learnedCount / $totalWords"
                progressBar.progress = (learnedCount.toFloat() / totalWords.toFloat() * 100).toInt()

                if (errorCount > 0) {
                    btnErrorReview.visibility = View.VISIBLE
                    btnErrorReview.text = "Hatalı Kelimeler ($errorCount)"
                } else {
                    btnErrorReview.visibility = View.GONE
                }
            }
        }
    }
}