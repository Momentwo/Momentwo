package cord.eoeo.momentwo.convention

import com.android.build.api.dsl.CommonExtension
import org.gradle.api.Project
import org.gradle.api.artifacts.VersionCatalog
import org.gradle.kotlin.dsl.dependencies

internal fun Project.configureAndroidCompose(
    commonExtension: CommonExtension<*, *, *, *, *, *>,
) {
    val versions: VersionCatalog = libs

    commonExtension.apply {
        buildFeatures {
            compose = true
        }
    }

    dependencies {
        val composeBom = versions.findLibrary("compose-bom").get()
        add("implementation", platform(composeBom))
        add("androidTestImplementation", platform(composeBom))

        add("implementation", versions.findLibrary("ui").get())
        add("implementation", versions.findLibrary("ui-graphics").get())
        add("implementation", versions.findLibrary("ui-tooling-preview").get())
        add("implementation", versions.findLibrary("material3").get())

        add("debugImplementation", versions.findLibrary("ui-tooling").get())
    }
}
