plugins {
    id("momentwo.android.library")
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "cord.eoeo.momentwo.feature.friend.api"
}

dependencies {
    implementation(libs.kotlin.serialization)
    testImplementation(libs.junit)
}
