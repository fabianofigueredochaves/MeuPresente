import org.gradle.kotlin.dsl.implementation
import org.gradle.kotlin.dsl.invoke

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
  //  id("com.android.application")
    id("com.google.devtools.ksp") version "2.1.21-2.0.1"
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.meupresente"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.meupresente"
        minSdk = 34
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {

    // Navigation Compose
    implementation("androidx.navigation:navigation-compose:2.9.3")

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    //implementation(libs.firebase.bom)
   // implementation(libs.firebase.analytics)

    // Firebase platform (BOM) para gerenciar as versões das bibliotecas
    //implementation(platform(libs.firebase.bom:'32''3''1')) // <--- VERIFIQUE A VERSÃO AQUI
    implementation(platform(libs.firebase.bom))
    // Firebase Authentication
    //implementation 'com.google.firebase:firebase-auth-ktx' // <--- ESSA É A DEPENDÊNCIA QUE ADICIONA FirebaseAuth
    // Firebase Cloud Firestore
    //implementation 'com.google.firebase:firebase-firestore-ktx' // <--- ESSA É A DEPENDÊNCIA QUE ADICIONA FirebaseFirestore
    implementation(libs.firebase.firestore)
    implementation(libs.firebase.auth.ktx)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)

    val roomVersion = "2.7.2"
    implementation("androidx.room:room-runtime:$roomVersion")
    ksp("androidx.room:room-compiler:$roomVersion") // Use ksp em vez de kapt
    implementation("androidx.room:room-ktx:$roomVersion") // Suporte a Coroutines

    // ViewModel Compose
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.2")
}