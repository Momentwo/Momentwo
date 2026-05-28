package cord.eoeo.momentwo.convention

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.dependencies

class AndroidNetworkConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            dependencies {
                add("implementation", libs.findLibrary("retrofit").get())
                add("implementation", libs.findLibrary("moshi").get())
                add("implementation", libs.findLibrary("moshi-converter").get())
                add("implementation", platform(libs.findLibrary("okhttp-bom").get()))
                add("implementation", libs.findLibrary("okhttp").get())
                add("implementation", libs.findLibrary("okhttp-logging-interceptor").get())
            }
        }
    }
}
