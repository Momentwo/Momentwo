plugins {
    alias(libs.plugins.momentwo.android.feature)
}

android {
    namespace = "cord.eoeo.momentwo.feature.album.impl"
    testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
    implementation(libs.lifecycle.viewmodel.savedstate)
    testImplementation(project(":core:testing"))
    testImplementation(libs.paging)
    implementation(project(":feature:album:api"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(libs.activity.compose)
    implementation(libs.icons.extended)
    implementation(libs.coil)
    implementation(libs.coil.compose)
}
