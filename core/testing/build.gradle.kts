plugins {
    id("momentwo.android.library")
}

android {
    namespace = "cord.eoeo.momentwo.core.testing"
}

dependencies {
    api(libs.junit)
    api(libs.coroutines.test)
}
