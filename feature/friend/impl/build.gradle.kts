plugins {
    id("momentwo.android.feature")
}

android {
    namespace = "cord.eoeo.momentwo.feature.friend.impl"
    testOptions.unitTests.isReturnDefaultValues = true
}

dependencies {
    testImplementation(project(":core:testing"))
    implementation(project(":feature:friend:api"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(libs.icons.extended)
    implementation(libs.coil)
    implementation(libs.paging)
    implementation(libs.paging.compose)
}
