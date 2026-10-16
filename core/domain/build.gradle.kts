plugins {
    id("momentwo.android.library")
    id("momentwo.android.hilt")
}

android {
    namespace = "cord.eoeo.momentwo.core.domain"
}

dependencies {
    implementation(project(":core:common"))
    implementation(libs.coroutines.core)
    testImplementation(project(":core:testing"))
    implementation(project(":core:data"))
    implementation(project(":core:model"))
    implementation(project(":core:datastore"))
}
