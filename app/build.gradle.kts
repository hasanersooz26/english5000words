plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("kotlin-kapt") // Room veritabanı için gerekli
}

android {
    namespace = "com.hasaniko.uygulama"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.hasaniko.uygulama"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    // Görseldeki "Aar Dependency compatibility issues" hatasını çözen kritik blok
    configurations.all {
        resolutionStrategy {
            force("androidx.core:core-ktx:1.12.0")
            force("androidx.appcompat:appcompat:1.6.1")
        }
    }
}

dependencies {
    // Temel AndroidX kütüphaneleri
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")

    // Room Veritabanı (Yerel veri saklama için)
    val roomVersion = "2.6.1"
    implementation("androidx.room:room-runtime:$roomVersion")
    implementation("androidx.room:room-ktx:$roomVersion")
    kapt("androidx.room:room-compiler:$roomVersion")

    // Coroutines (Arka plan işlemleri için)
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")

    // GSON (JSON verilerini işlemek için)
    implementation("com.google.code.gson:gson:2.10.1")

    // Test kütüphaneleri
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}