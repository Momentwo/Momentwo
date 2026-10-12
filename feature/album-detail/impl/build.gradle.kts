plugins {
    alias(libs.plugins.momentwo.android.feature)
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "cord.eoeo.momentwo.feature.albumdetail.impl"
}

dependencies {
    testImplementation(libs.junit)
    implementation(project(":feature:subalbum:api"))
    implementation(project(":feature:album-detail:api"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(libs.activity.compose)
    implementation(libs.icons.extended)
    implementation(libs.coil)
    implementation(libs.lifecycle.viewmodel.savedstate)
}
