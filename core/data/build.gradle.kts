plugins {
    alias(libs.plugins.momentwo.android.library)
    alias(libs.plugins.momentwo.android.hilt)
}

android {
    namespace = "cord.eoeo.momentwo.core.data"
}

dependencies {
    testImplementation(project(":core:testing"))
    implementation(project(":core:common"))
    implementation(project(":core:model"))
    implementation(project(":core:network"))
    implementation(project(":core:database"))
    implementation(project(":core:datastore"))

    implementation(libs.datastore)
    implementation(libs.room)
    implementation(libs.room.ktx)
    implementation(libs.retrofit)
    implementation(libs.moshi)
    implementation(platform(libs.okhttp.bom))
    implementation(libs.okhttp)
    implementation(libs.paging)
    implementation(libs.coil)
    implementation(libs.coroutines.core)
}
