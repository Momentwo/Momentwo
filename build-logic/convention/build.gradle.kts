import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
}

group = "cord.eoeo.momentwo.buildlogic"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

kotlin.compilerOptions.jvmTarget.set(JvmTarget.JVM_17)

tasks.withType<JavaCompile>().configureEach {
    options.release.set(17)
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(gradleTestKit())
    compileOnly(libs.android.gradlePlugin)
    compileOnly(libs.kotlin.gradlePlugin)
    compileOnly(libs.ksp.gradlePlugin)
    compileOnly(libs.compose.compiler.gradlePlugin)
    compileOnly(libs.hilt.gradlePlugin)
    compileOnly(libs.room.gradlePlugin)
}

tasks.test {
    systemProperty("momentwo.test.agp.version", libs.versions.agp.get())
}

gradlePlugin {
    plugins {
        register("moduleArchitecture") {
            id = "momentwo.module-architecture"
            implementationClass = "cord.eoeo.momentwo.convention.architecture.ModuleArchitecturePlugin"
        }
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
