plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.edu.english"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.edu.english"
        minSdk = 24
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
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.drawerlayout)
    
    // RecyclerView for animal grid
    implementation(libs.recyclerview)
    
    // AR/Sceneform for 3D models
    implementation(libs.sceneform)
    implementation(libs.gridlayout)
    
    // Lottie for beautiful animations
    implementation("com.airbnb.android:lottie:6.3.0")
    
    // CardView for better UI
    implementation("androidx.cardview:cardview:1.0.0")
    
    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")
    
    // ═══════════════════════════════════════════════════════════════
    // 🎵 MAGIC MELODY DEPENDENCIES
    // ═══════════════════════════════════════════════════════════════
    
    // Lifecycle (ViewModel, LiveData)
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.livedata)
    
    // Room Database for persistence
    implementation(libs.room.runtime)
    annotationProcessor(libs.room.compiler)
    
    // ExoPlayer (Media3) for audio playback
    implementation(libs.media3.exoplayer)
    implementation(libs.media3.ui)
    
    // CameraX for AR Boss Battle
    implementation(libs.camerax.core)
    implementation(libs.camerax.camera2)
    implementation(libs.camerax.lifecycle)
    implementation(libs.camerax.view)
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}