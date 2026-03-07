package com.hasaniko.uygulama

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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

        // MainActivity'den (Dialog üzerinden) gelen ayarları alıyoruz
        // level: 0 ise karışık, 1-2-3 ise o bölüme özgü
        val level = intent.getIntExtra("level", 0)
        val limit = intent.getIntExtra("limit", 10) // Kullanıcının belirlediği dinamik sayı

        setupViews()
        loadQuizData(level, limit)
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

    private fun loadQuizData(level: Int, limit: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            // Belirli bir seviye seçildiyse sadece oradan (Örn: Bölüm 1), seçilmediyse tümünden getir
            quizList = if (level == 0) {
                db.wordDao().getRandomQuizWords(limit)
            } else {
                db.wordDao().getQuizWordsByLevel(level, limit)
            }

            withContext(Dispatchers.Main) {
                if (quizList.isNotEmpty()) {
                    progressBar.max = quizList.size // Barın maksimum değerini soru sayısına eşitle
                    showQuestion()
                } else {
                    Toast.makeText(this@QuizActivity, "Seçili seviyede kelime bulunamadı!", Toast.LENGTH_SHORT).show()
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

        // Üstteki görsel ilerleme çubuğunu ve metni güncelle
        progressBar.progress = currentIndex + 1
        tvProgressInfo.text = "${currentIndex + 1} / ${quizList.size}"

        // %50 ihtimalle İngilizce sor, %50 ihtimalle Türkçe sor
        isEnglishQuestion = Random.nextBoolean()
        tvWord.text = if (isEnglishQuestion) currentWordObj.word else currentWordObj.meaning

        // Şıkları hazırla (1 doğru + 3 rastgele yanlış)
        CoroutineScope(Dispatchers.IO).launch {
            val options = db.wordDao().getRandomQuizWords(4).toMutableList()
            // Doğru cevap şıklar arasında yoksa, rastgele birinin yerine doğruyu koy
            if (!options.any { it.id == currentWordObj.id }) {
                options[0] = currentWordObj
            }
            val shuffledOptions = options.shuffled()

            withContext(Dispatchers.Main) {
                for (i in buttons.indices) {
                    // Soru yönüne göre şıkların dilini ayarla
                    buttons[i].text = if (isEnglishQuestion) shuffledOptions[i].meaning else shuffledOptions[i].word

                    // Görseli sıfırla
                    buttons[i].setBackgroundColor(Color.WHITE)
                    buttons[i].setTextColor(Color.parseColor("#102A43"))
                    buttons[i].isEnabled = true

                    buttons[i].setOnClickListener { checkAnswer(shuffledOptions[i], buttons[i]) }
                }
            }
        }
    }

    private fun checkAnswer(selected: Word, clickedBtn: Button) {
        // Ardarda basılmasını önlemek için kilitler
        buttons.forEach { it.isEnabled = false }
        val correct = quizList[currentIndex]

        if (selected.id == correct.id) {
            // Doğru: Yeşil yanar
            clickedBtn.setBackgroundColor(Color.parseColor("#4CAF50"))
            clickedBtn.setTextColor(Color.WHITE)
            correctCount++
        } else {
            // Yanlış: Kırmızı yanar
            clickedBtn.setBackgroundColor(Color.parseColor("#F44336"))
            clickedBtn.setTextColor(Color.WHITE)
            wrongWordsList.add(correct) // Hangi bölümlerde eksiği olduğunu söylemek için listeye ekle
            updateErrorInDb(correct)
        }

        // Sonucun görülmesi için kısa bir bekleme süresi
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
        // Kelime türlerine göre hata analizi (Eksiği olduğu bölümler)
        val analysis = wrongWordsList.groupBy { it.type }
            .map { "${it.key}: ${it.value.size} hata" }
            .joinToString("\n")

        val resultSummary = "Doğru Sayısı: $correctCount / ${quizList.size}"
        val analysisMsg = if (analysis.isEmpty()) "Hatanız yok, mükemmel ilerliyorsunuz!" else "Eksik olduğunuz türler:\n$analysis"

        // Daha şık bir sonuç için AlertDialog kullanıyoruz
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