plugins {
    alias(libs.plugins.momentwo.android.feature)
    alias(libs.plugins.kotlinSerialization)
}

android {
    namespace = "cord.eoeo.momentwo.feature.albumdetail.impl"
    testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
    testImplementation(project(":core:testing"))
    testImplementation(libs.paging)
    implementation(project(":feature:subalbum:api"))
    implementation(project(":feature:album-detail:api"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(libs.activity.compose)
    implementation(libs.icons.extended)
    implementation(libs.coil.compose)
    implementation(libs.coil)
    implementation(libs.lifecycle.viewmodel.savedstate)
}
