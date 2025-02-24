plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    id("com.google.devtools.ksp") version "1.9.0-1.0.11"
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

    kotlinOptions {
        jvmTarget = "17"
    }

    viewBinding {
        enable = true
    }
}

dependencies {

    // Core Android
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // Testing
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
    implementation(libs.androidx.exifinterface)

    // Room (SQLite) para base de datos local
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    // Usa KSP en lugar de KAPT para el compilador de Room
    ksp(libs.room.compiler)

    // Corrutinas para ejecución en segundo plano
    implementation(libs.coroutines.android)

    // WorkManager para sincronización en segundo plano
    implementation(libs.workmanager)

    // Geolocalización (FusedLocationProvider)
    implementation(libs.play.services.location)

    // ViewModel y LiveData para manejar la UI
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.lifecycle.livedata.ktx)
}

ksp {
    // Configuración opcional para Room
    arg("room.incremental", "true")
    arg("room.schemaLocation", "$projectDir/schemas")
}
