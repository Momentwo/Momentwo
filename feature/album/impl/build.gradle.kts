plugins {
    alias(libs.plugins.momentwo.android.feature)
}

android {
    namespace = "cord.eoeo.momentwo.feature.album.impl"
}

dependencies {
    implementation(project(":feature:album:api"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(libs.activity.compose)
    implementation(libs.icons.extended)
    implementation(libs.coil)
}
