plugins {
    alias(libs.plugins.momentwo.android.application)
    alias(libs.plugins.momentwo.android.application.compose)
    alias(libs.plugins.momentwo.android.hilt)
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
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    // The app owns root navigation and composition across features.
    implementation(project(":feature:auth:api"))
    implementation(project(":feature:auth:impl"))
    implementation(project(":feature:friend:api"))
    implementation(project(":feature:friend:impl"))
    implementation(project(":feature:subalbum:impl"))
    implementation(project(":feature:album-detail:api"))
    implementation(project(":feature:album-detail:impl"))
    implementation(project(":feature:album:api"))
    implementation(project(":feature:album:impl"))
    implementation(project(":feature:photo:api"))
    implementation(project(":feature:photo:impl"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))

    implementation(libs.core.ktx)
    implementation(libs.activity.compose)
    implementation(libs.navigation.compose)
    implementation(libs.coil)
    implementation(libs.coroutines.core)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.ui.test.junit4)
    debugImplementation(libs.ui.test.manifest)
}
