plugins {
    alias(libs.plugins.momentwo.android.feature)
}

android {
    namespace = "cord.eoeo.momentwo.feature.photo.impl"
    testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
    testImplementation(project(":core:testing"))
    implementation(project(":feature:photo:api"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(libs.activity.compose)
    implementation(libs.icons.extended)
    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.paging)
    implementation(libs.paging.compose)
    implementation(libs.lifecycle.viewmodel.savedstate)
}
