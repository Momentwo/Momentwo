plugins {
    id("momentwo.android.library")
    id("momentwo.android.hilt")
}

android {
    namespace = "cord.eoeo.momentwo.core.datastore"
}

dependencies {
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(libs.coroutines.core)
    testImplementation(project(":core:testing"))
    implementation(libs.datastore)
}
