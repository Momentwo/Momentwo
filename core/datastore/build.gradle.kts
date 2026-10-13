plugins {
    alias(libs.plugins.momentwo.android.library)
    alias(libs.plugins.momentwo.android.hilt)
}

android {
    namespace = "cord.eoeo.momentwo.core.datastore"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.coroutines.core)
    testImplementation(project(":core:testing"))
    implementation(libs.datastore)
}
