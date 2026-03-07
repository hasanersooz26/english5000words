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
    private lateinit var tvErrorCount: TextView
    private lateinit var errorBadge: LinearLayout
    private lateinit var tvCurrentLevel: TextView
    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        db = AppDatabase.getInstance(this)
        DatabaseInitializer.populateDatabase(this, db)

        tvErrorCount = findViewById(R.id.tvErrorCount)
        errorBadge = findViewById(R.id.errorBadge)
        tvCurrentLevel = findViewById(R.id.tvCurrentLevel)
        progressBar = findViewById(R.id.levelProgressBar)

        findViewById<Button>(R.id.btnResume).setOnClickListener {
            val intent = Intent(this, LearningActivity::class.java)
            intent.putExtra("mode", "continue")
            startActivity(intent)
        }

        findViewById<Button>(R.id.btnLearn).setOnClickListener {
            showLevelSelectDialog()
        }

        findViewById<Button>(R.id.btnQuiz).setOnClickListener {
            showQuizSetupDialog()
        }

        errorBadge.setOnClickListener {
            val intent = Intent(this, LearningActivity::class.java)
            intent.putExtra("mode", "error_review")
            startActivity(intent)
        }
    }

    private fun showLevelSelectDialog() {
        CoroutineScope(Dispatchers.IO).launch {
            val maxFinished = db.wordDao().getMaxFinishedLevel() ?: 0
            val currentSelectableLevel = maxFinished + 1

            withContext(Dispatchers.Main) {
                val levels = arrayOf("Bölüm 1", "Bölüm 2", "Bölüm 3")
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Bölüm Seçin")
                    .setItems(levels) { _, which ->
                        val selectedLevel = which + 1
                        if (selectedLevel <= currentSelectableLevel) {
                            val intent = Intent(this@MainActivity, LearningActivity::class.java)
                            intent.putExtra("level", selectedLevel)
                            startActivity(intent)
                        } else {
                            Toast.makeText(this@MainActivity, "Önceki bölümü bitirmelisiniz!", Toast.LENGTH_SHORT).show()
                        }
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
            val maxFinished = db.wordDao().getMaxFinishedLevel() ?: 0
            val activeLevel = maxFinished + 1

            withContext(Dispatchers.Main) {
                val levelsList = mutableListOf("Öğrendiklerim (Karışık)")
                for (i in 1..activeLevel) { levelsList.add("Bölüm $i") }

                spinnerLevel.adapter = ArrayAdapter(this@MainActivity, android.R.layout.simple_spinner_dropdown_item, levelsList)

                builder.setView(view)
                builder.setPositiveButton("Başlat") { _, _ ->
                    val limitInput = etLimit.text.toString()
                    val limit = if (limitInput.isEmpty()) 10 else limitInput.toInt()

                    val intent = Intent(this@MainActivity, QuizActivity::class.java)
                    intent.putExtra("level", spinnerLevel.selectedItemPosition)
                    intent.putExtra("limit", limit)
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
                    errorBadge.visibility = View.VISIBLE
                    tvErrorCount.text = errorCount.toString()
                } else {
                    errorBadge.visibility = View.GONE
                }
            }
        }
    }
}