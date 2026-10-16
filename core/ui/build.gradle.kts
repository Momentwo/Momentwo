plugins {
    id("momentwo.android.library")
    id("momentwo.android.library.compose")
}

android {
    namespace = "cord.eoeo.momentwo.core.ui"
}

dependencies {
    // Public UI functions expose WindowInsetsControllerCompat from AndroidX Core.
    api(libs.core.ktx)
    implementation(project(":core:common"))
    implementation(libs.lifecycle.runtime.compose)
    implementation(libs.coroutines.core)
    testImplementation(project(":core:testing"))
    implementation(project(":core:designsystem"))
    implementation(project(":core:model"))

    implementation(libs.icons)
    implementation(libs.icons.extended)
    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.paging)
    implementation(libs.paging.compose)
}
