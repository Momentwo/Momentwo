plugins {
    id("momentwo.android.feature")
}

android {
    namespace = "cord.eoeo.momentwo.feature.photo.impl"
    testNamespace = "cord.eoeo.momentwo.photo.migrationcheck.test"
    defaultConfig.testApplicationId = "cord.eoeo.momentwo.photo.migrationcheck.test"
    defaultConfig.testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
    androidTestImplementation(project(":core:testing"))
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.runner)
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
