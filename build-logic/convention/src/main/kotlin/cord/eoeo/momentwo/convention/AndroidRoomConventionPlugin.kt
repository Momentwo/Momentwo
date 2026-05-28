package cord.eoeo.momentwo.convention

import androidx.room.gradle.RoomExtension
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.configure
import org.gradle.kotlin.dsl.dependencies

class AndroidRoomConventionPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        with(target) {
            with(pluginManager) {
                apply("com.google.devtools.ksp")
                apply("androidx.room")
            }

            extensions.configure<RoomExtension> {
                schemaDirectory("$projectDir/schemas")
            }

            dependencies {
                add("implementation", libs.findLibrary("room").get())
                add("implementation", libs.findLibrary("room-ktx").get())
                add("implementation", libs.findLibrary("room-paging").get())
                add("ksp", libs.findLibrary("room-compiler").get())
            }
        }
    }
}
