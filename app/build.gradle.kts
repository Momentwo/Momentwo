import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.momentwo.android.application)
    alias(libs.plugins.momentwo.android.application.compose)
    alias(libs.plugins.momentwo.android.hilt)
    alias(libs.plugins.momentwo.android.room)
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "cord.eoeo.momentwo"

    defaultConfig {
        applicationId = "cord.eoeo.momentwo"
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }

        buildConfigField("String", "BASE_URL", getLocalProperty("BASE_URL"))
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

fun getLocalProperty(key: String): String = gradleLocalProperties(rootDir, providers).getProperty(key)

dependencies {
    implementation(project(":core:designsystem"))

    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.icons)
    implementation(libs.icons.extended)

    // Lifecycle
    implementation(libs.lifecycle.runtime)
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.lifecycle.viewmodel)
    implementation(libs.lifecycle.viewmodel.compose)
    implementation(libs.lifecycle.viewmodel.savedstate)

    // Navigation Compose
    implementation(libs.navigation.compose)

    // Hilt navigation compose (hilt-android / compiler 는 momentwo.android.hilt 가 추가)
    implementation(libs.hilt.compose)

    // Retrofit / OkHttp / Moshi
    implementation(libs.retrofit)
    implementation(libs.moshi)
    implementation(libs.moshi.converter)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

    // Coil
    implementation(libs.coil)
    implementation(libs.coil.compose)

    // DataStore
    implementation(libs.datastore)

    // Kotlin Serialization
    implementation(libs.kotlin.serialization)

    // Paging3 (room-paging 은 momentwo.android.room 이 추가)
    implementation(libs.paging)
    implementation(libs.paging.compose)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.test.manifest)
}
