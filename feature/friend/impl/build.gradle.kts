plugins {
    alias(libs.plugins.momentwo.android.feature)
}

android {
    namespace = "cord.eoeo.momentwo.feature.friend.impl"
}

dependencies {
    implementation(project(":feature:friend:api"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:data"))
    implementation(libs.icons.extended)
    implementation(libs.coil)
    implementation(libs.paging)
    implementation(libs.paging.compose)
}
