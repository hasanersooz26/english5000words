# English 5000 Words

Android üzerinde çevrimdışı kelime çalışmak için geliştirilmiş, seviye tabanlı bir öğrenme ve quiz uygulaması.

## Özellikler

- Kelimeleri seviyelere göre çalışma
- Kaldığın yerden devam etme ve ilerleme takibi
- Seviye bazlı veya karışık quiz oluşturma
- Yanlış cevaplanan kelimeleri yeniden çalışma
- Özel soru bankası ve quiz modu
- Verileri cihazda Room ile çevrimdışı saklama

## Teknolojiler

![Kotlin](https://img.shields.io/badge/Kotlin-7F52FF?logo=kotlin&logoColor=white)
![Android](https://img.shields.io/badge/Android-3DDC84?logo=android&logoColor=white)
![Room](https://img.shields.io/badge/Room-SQLite-003B57?logo=sqlite&logoColor=white)
![Coroutines](https://img.shields.io/badge/Coroutines-Asynchronous-0095D5)

## Yerel kurulum

1. Projeyi Android Studio ile açın.
2. Gradle eşitlemesinin tamamlanmasını bekleyin.
3. Android 7.0 veya üzeri bir emülatör ya da cihaz seçin.
4. Uygulamayı çalıştırın.

Komut satırından debug derlemesi:

```bash
./gradlew assembleDebug
```

> Proje aktif geliştirme aşamasındadır. Kelime verileri uygulama paketindeki JSON dosyalarından ilk açılışta yerel veritabanına aktarılır.
