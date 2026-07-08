import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    alias(libs.plugins.momentwo.android.library)
    alias(libs.plugins.momentwo.android.network)
    alias(libs.plugins.momentwo.android.hilt)
}

android {
    namespace = "cord.eoeo.momentwo.core.network"

    defaultConfig {
        buildConfigField("String", "BASE_URL", getLocalProperty("BASE_URL"))
    }

    buildFeatures {
        buildConfig = true
    }
}

fun getLocalProperty(key: String): String = gradleLocalProperties(rootDir, providers).getProperty(key)

dependencies {
    implementation(project(":core:datastore"))

    implementation(libs.coroutines.core)
}
