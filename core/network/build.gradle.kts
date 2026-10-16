import java.util.Properties

plugins {
    alias(libs.plugins.momentwo.android.library)
    alias(libs.plugins.momentwo.android.network)
    alias(libs.plugins.momentwo.android.hilt)
}

val localProperties = providers.fileContents(rootProject.layout.projectDirectory.file("local.properties"))
    .asText.map { contents -> Properties().apply { contents.reader().use { load(it) } } }

android {
    namespace = "cord.eoeo.momentwo.core.network"

    defaultConfig {
        buildConfigField("String", "BASE_URL", localProperties.map { properties ->
            requireNotNull(properties.getProperty("BASE_URL")) {
                "Set the quoted BASE_URL property in local.properties"
            }
        }.get())
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(project(":core:datastore"))
    implementation(project(":core:model"))

    implementation(libs.coroutines.core)
}
