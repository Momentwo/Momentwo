plugins {
    `kotlin-dsl`
}

group = "cord.eoeo.momentwo.buildlogic"

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

dependencies {
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
}

gradlePlugin {
    plugins {
        register("androidApplication") {
            id = "momentwo.android.application"
            implementationClass = "cord.eoeo.momentwo.convention.AndroidApplicationConventionPlugin"
        }
        register("androidApplicationCompose") {
            id = "momentwo.android.application.compose"
            implementationClass = "cord.eoeo.momentwo.convention.AndroidApplicationComposeConventionPlugin"
        }
        register("androidLibrary") {
            id = "momentwo.android.library"
            implementationClass = "cord.eoeo.momentwo.convention.AndroidLibraryConventionPlugin"
        }
        register("androidLibraryCompose") {
            id = "momentwo.android.library.compose"
            implementationClass = "cord.eoeo.momentwo.convention.AndroidLibraryComposeConventionPlugin"
        }
        register("androidFeature") {
            id = "momentwo.android.feature"
            implementationClass = "cord.eoeo.momentwo.convention.AndroidFeatureConventionPlugin"
        }
        register("androidHilt") {
            id = "momentwo.android.hilt"
            implementationClass = "cord.eoeo.momentwo.convention.AndroidHiltConventionPlugin"
        }
        register("androidRoom") {
            id = "momentwo.android.room"
            implementationClass = "cord.eoeo.momentwo.convention.AndroidRoomConventionPlugin"
        }
        register("androidNetwork") {
            id = "momentwo.android.network"
            implementationClass = "cord.eoeo.momentwo.convention.AndroidNetworkConventionPlugin"
        }
        register("jvmLibrary") {
            id = "momentwo.jvm.library"
            implementationClass = "cord.eoeo.momentwo.convention.JvmLibraryConventionPlugin"
        }
    }
}
