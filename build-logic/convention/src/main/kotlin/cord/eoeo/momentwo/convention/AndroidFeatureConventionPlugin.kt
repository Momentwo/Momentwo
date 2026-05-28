package cord.eoeo.momentwo.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidFeatureConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("momentwo.android.library")
                apply("momentwo.android.library.compose")
                apply("momentwo.android.hilt")
            }

            dependencies {
                add("implementation", libs.findLibrary("lifecycle-runtime").get())
                add("implementation", libs.findLibrary("lifecycle-runtime-compose").get())
                add("implementation", libs.findLibrary("lifecycle-viewmodel").get())
                add("implementation", libs.findLibrary("lifecycle-viewmodel-compose").get())
                add("implementation", libs.findLibrary("navigation-compose").get())
                add("implementation", libs.findLibrary("hilt-compose").get())
                add("implementation", libs.findLibrary("kotlin-serialization").get())
            }
        }
    }
}
