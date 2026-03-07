package com.hasaniko.uygulama

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.concurrent.Executors

object DatabaseInitializer {

    fun populateDatabase(context: Context, db: AppDatabase) {
        val prefs = context.getSharedPreferences("AppPrefs", Context.MODE_PRIVATE)
        val isDataLoaded = prefs.getBoolean("isDataLoaded", false)

        // Eğer veriler daha önce yüklenmediyse işlemi başlat
        if (!isDataLoaded) {
            val executor = Executors.newSingleThreadExecutor()
            executor.execute {
                try {
                    // Assets klasöründeki JSON dosyasını aç
                    val inputStream = context.assets.open("kelimeler.json")
                    val size = inputStream.available()
                    val buffer = ByteArray(size)
                    inputStream.read(buffer)
                    inputStream.close()
                    val json = String(buffer, Charsets.UTF_8)

                    // JSON verisini Word listesine dönüştür
                    val gson = Gson()
                    val listType = object : TypeToken<List<Word>>() {}.type
                    val words: List<Word> = gson.fromJson(json, listType)

                    // Veritabanına topluca ekle
                    db.wordDao().insertAll(words)

                    // Bir daha yüklenmemesi için işareti kaydet
                    prefs.edit().putBoolean("isDataLoaded", true).apply()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}