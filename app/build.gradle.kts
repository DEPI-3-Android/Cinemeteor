import java.io.FileInputStream
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
    id("kotlin-parcelize")
}

// Load local.properties file
val localProperties = Properties()
val localPropertiesFile = rootProject.file("local.properties")
if (localPropertiesFile.exists()) {
    localPropertiesFile.inputStream().use { localProperties.load(it) }
}

android {
    namespace = "com.acms.cinemeteor"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.acms.cinemeteor"
        minSdk = 29
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        
        // Load TMDB API key from local.properties
        val tmdbApiKey = localProperties.getProperty("TMDB_API_KEY", "").trim()
        if (tmdbApiKey.isNotEmpty()) {
            buildConfigField("String", "TMDB_API_KEY", "\"$tmdbApiKey\"")
            println("TMDB_API_KEY loaded successfully (length: ${tmdbApiKey.length})")
        } else {
            buildConfigField("String", "TMDB_API_KEY", "\"\"")
            println("WARNING: TMDB_API_KEY not found in local.properties!")
        }

        // Load IMGBB API key from local.properties
        val IMGBBApiKey = localProperties.getProperty("IMGBB_API_KEY", "").trim()
        if (IMGBBApiKey.isNotEmpty()) {
            buildConfigField("String", "IMGBB_API_KEY", "\"$IMGBBApiKey\"")
            println("IMGBB_API_KEY loaded successfully (length: ${IMGBBApiKey.length})")
        } else {
            buildConfigField("String", "IMGBB_API_KEY", "\"\"")
            println("WARNING: IMGBB_API_KEY not found in local.properties!")
        }
    }
    
    buildFeatures {
        compose = true
        buildConfig = true
    }
    val keyStoreProperties = Properties()
    val propertiesFile = rootProject.file("local.properties")
    if (propertiesFile.exists()) {
        keyStoreProperties.load(FileInputStream(propertiesFile))
    }
    signingConfigs {
        create("sharedDebug") {
            storeFile = file("debug.keystore")
            storePassword = keyStoreProperties.getProperty("STORE_PASSWORD")
            keyAlias = keyStoreProperties.getProperty("STORE_ALIAS")
            keyPassword = keyStoreProperties.getProperty("KEY_PASSWORD")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            signingConfig = signingConfigs.getByName("sharedDebug")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.9.4")
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation("androidx.compose.material3:material3:1.2.0")
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.navigation.compose)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    implementation(platform("com.google.firebase:firebase-bom:34.6.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-analytics")
    implementation("com.google.firebase:firebase-firestore")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation("androidx.compose.animation:animation")
    implementation("io.coil-kt:coil-compose:2.6.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.27.0")
    implementation("com.google.accompanist:accompanist-swiperefresh:0.27.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.appcompat:appcompat-resources:1.7.1")
    implementation("androidx.credentials:credentials-play-services-auth:1.3.0")
    implementation("androidx.credentials:credentials:1.3.0")
    implementation("com.google.android.libraries.identity.googleid:googleid:1.1.1")

    // Retrofit and networking
    implementation(libs.retrofit)
    implementation(libs.retrofit.gson)
    implementation(libs.gson)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
}