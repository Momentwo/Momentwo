plugins {
    alias(libs.plugins.momentwo.android.library)
    alias(libs.plugins.momentwo.android.library.compose)
}

android {
    namespace = "cord.eoeo.momentwo.core.ui"
}

dependencies {
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))

    implementation(libs.icons)
    implementation(libs.icons.extended)
    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.paging)
    implementation(libs.paging.compose)
}
