plugins {
    alias(libs.plugins.android.application)
}

import java.util.Properties

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
        
        // Room schema export
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
        
        // Read Gemini API key from local.properties for debug builds
        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }
        buildConfigField("String", "GEMINI_API_KEY", "\"${localProperties.getProperty("GEMINI_API_KEY", "")}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            // Empty API key for release builds - use Firebase instead
            buildConfigField("String", "GEMINI_API_KEY", "\"\"")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    
    buildFeatures {
        buildConfig = true
        viewBinding = true
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
    
    // Room Database for offline storage
    implementation("androidx.room:room-runtime:2.6.1")
    annotationProcessor("androidx.room:room-compiler:2.6.1")
    
    // ViewModel and LiveData
    implementation("androidx.lifecycle:lifecycle-viewmodel:2.7.0")
    implementation("androidx.lifecycle:lifecycle-livedata:2.7.0")
    implementation("androidx.lifecycle:lifecycle-common-java8:2.7.0")
    
    // OkHttp for REST API calls
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    // Gson for JSON parsing
    implementation("com.google.code.gson:gson:2.10.1")
    
    // Fragment KTX for better fragment handling
    implementation("androidx.fragment:fragment:1.6.2")
    
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}