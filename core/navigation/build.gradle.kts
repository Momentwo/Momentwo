plugins {
    alias(libs.plugins.momentwo.android.library)
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "cord.eoeo.momentwo.core.navigation"
}

dependencies {
    implementation(libs.kotlin.serialization)
}
