plugins {
    alias(libs.plugins.momentwo.android.feature)
}

android {
    namespace = "cord.eoeo.momentwo.feature.subalbum.impl"
}

dependencies {
    implementation(project(":feature:subalbum:api"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(libs.activity.compose)
    implementation(libs.coil)
}
