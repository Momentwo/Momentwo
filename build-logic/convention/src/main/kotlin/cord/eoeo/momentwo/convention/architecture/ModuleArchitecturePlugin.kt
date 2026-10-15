package cord.eoeo.momentwo.convention.architecture

import org.gradle.api.Plugin
import org.gradle.api.Project

class ModuleArchitecturePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        require(target == target.rootProject) { "momentwo.module-architecture must be applied to the root project" }
        require(!target.gradle.startParameter.isConfigureOnDemand) {
            "Architecture checks cannot run with --configure-on-demand: all module configurations must be evaluated. Disable that option."
        }
        target.pluginManager.apply("base")
        val boundaries = target.tasks.register("checkModuleBoundaries", ArchitectureTask::class.java) {
            mode.set("boundaries")
            rootDirectory.set(target.layout.projectDirectory)
            reportDirectory.set(target.layout.buildDirectory.dir("reports/module-architecture"))
        }
        val cycles = target.tasks.register("checkModuleCycles", ArchitectureTask::class.java) {
            mode.set("cycles")
            rootDirectory.set(target.layout.projectDirectory)
            reportDirectory.set(target.layout.buildDirectory.dir("reports/module-architecture"))
        }
        val aggregate = target.tasks.register("checkArchitecture", ArchitectureTask::class.java) {
            group = "verification"
            description = "Checks module boundaries, imports and context-specific dependency cycles."
            mode.set("architecture")
            rootDirectory.set(target.layout.projectDirectory)
            reportDirectory.set(target.layout.buildDirectory.dir("reports/module-architecture"))
        }
        target.allprojects {
            tasks.matching { it.name == "check" }.configureEach { dependsOn(aggregate) }
        }
        // No task action retains Project/Configuration objects or resolves a classpath.
        target.gradle.projectsEvaluated {
            val model = ArchitectureCollector.collect(target)
            listOf(boundaries, cycles, aggregate).forEach { task -> task.configure {
                snapshot.set(model)
                model.sources.forEach { source ->
                    sourceFiles.from(target.fileTree(source.directory) {
                        include("**/*.kt", "**/*.java")
                        exclude("**/build/**", "**/generated/**", "**/.gradle/**")
                    })
                }
            } }
        }
    }
}
