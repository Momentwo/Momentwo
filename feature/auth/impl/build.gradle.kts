plugins {
    id("momentwo.android.feature")
}

android {
    namespace = "cord.eoeo.momentwo.feature.auth.impl"

    // Local ViewModel tests assert state/effects; framework logging is a no-op.
    testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
    implementation(project(":feature:auth:api"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(libs.activity.compose)
    implementation(libs.lifecycle.viewmodel.savedstate)
    implementation(libs.icons)
    implementation(libs.coil)
    testImplementation(project(":core:testing"))
    testImplementation(project(":core:datastore"))
}
