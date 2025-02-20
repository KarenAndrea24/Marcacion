plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
//    id("kotlin-kapt") // Habilitar Kapt para Room
    id("com.google.devtools.ksp") version "1.8.0-1.0.9" // Agrega esta línea
}

android {
    namespace = "com.example.marcacion"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.marcacion"
        minSdk = 26
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    viewBinding {
        enable = true
    }

    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // Retrofit para consumo de APIs
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)

    // CameraX para captura de fotos
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)

    // Room (SQLite) para base de datos local
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
//    kapt(libs.room.compiler) // Importante para generar código Room

    // Corrutinas para ejecución en segundo plano
    implementation(libs.coroutines.android)

    // WorkManager para sincronización en segundo plano
    implementation(libs.workmanager)

    // Geolocalización (FusedLocationProvider)
    implementation(libs.play.services.location)

    // Testing
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    // ViewModel y LiveData para manejar la UI
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)
}

ksp {
    arg("room.incremental", "true")
    arg("room.schemaLocation", "$projectDir/schemas")
}