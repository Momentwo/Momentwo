plugins {
    alias(libs.plugins.momentwo.android.library)
    alias(libs.plugins.momentwo.android.hilt)
}

android {
    namespace = "cord.eoeo.momentwo.core.common"
}

dependencies {
    implementation(libs.coroutines.core)
    testImplementation(project(":core:testing"))
    implementation(libs.lifecycle.viewmodel)
}
