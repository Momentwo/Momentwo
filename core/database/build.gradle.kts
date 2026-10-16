plugins {
    id("momentwo.android.library")
    id("momentwo.android.room")
    id("momentwo.android.hilt")
}

android {
    namespace = "cord.eoeo.momentwo.core.database"
}

dependencies {
    implementation(project(":core:model"))
}
