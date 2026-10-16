package cord.eoeo.momentwo.convention.architecture

import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.artifacts.ProjectDependency
import org.gradle.api.tasks.SourceSetContainer

internal object ArchitectureCollector {
    private data class Context(val configuration: Configuration, val node: Node, val buildType: String?)

    fun collect(root: Project): Snapshot {
        val projects = root.subprojects.filter { it.buildFile.isFile }.sortedBy { it.path }
        val modules = projects.map { project ->
            val android = project.pluginManager.hasPlugin("com.android.library") || project.pluginManager.hasPlugin("com.android.application")
            Module(project.path, if (android) AndroidArchitectureSources.namespace(project) else "", android)
        }
        val contexts = projects.associate { project -> project.path to project.configurations.mapNotNull { config ->
            context(project.path, config, modules.first { it.path == project.path }.android)
        } }
        val problems = mutableListOf<Finding>()
        val edges = mutableListOf<Edge>()
        val nodes = contexts.values.flatten().map { it.node }.distinct()
        val sources = mutableListOf<SourceRoot>()
        projects.forEach { project ->
            val module = modules.first { it.path == project.path }
            if (module.android) sources += AndroidArchitectureSources.roots(project)
            else project.extensions.findByType(SourceSetContainer::class.java)?.forEach { source ->
                val codeRoots = (source.allSource.srcDirs - source.resources.srcDirs) + source.allJava.srcDirs
                sources += codeRoots.map { SourceRoot(project.path, source.name, it.absolutePath) }
            }
            project.configurations.sortedBy { it.name }.forEach { origin ->
                origin.dependencies.withType(ProjectDependency::class.java).forEach dependencyLoop@{ dependency ->
                    val targetProject = root.findProject(dependency.path)
                    if (targetProject == null) {
                        problems += unsupported(project.path, origin.name, "Project dependency to ${dependency.path} is outside the checked build")
                        return@dependencyLoop
                    }
                    val consumers = contexts.getValue(project.path).filter { origin in it.configuration.hierarchy }
                    if (consumers.isEmpty()) {
                        problems += unsupported(project.path, origin.name, "Project dependency to ${dependency.path} is not attached to a supported compile/runtime context")
                    }
                    consumers.forEach { consumer ->
                        val target = selectTarget(dependency, targetProject, consumer, contexts[dependency.path].orEmpty())
                        if (target == null) problems += unsupported(project.path, origin.name,
                            "Cannot select ${dependency.path} output for ${consumer.node.key}; targetConfiguration=${dependency.targetConfiguration ?: "<variant-aware>"}, capabilities=${dependency.requestedCapabilities}, requestedAttributes=${dependency.attributes}")
                        else edges += Edge(consumer.node, target, origin.name)
                    }
                }
            }
        }
        nodes.filter { it.kind.isTest() }.forEach { test ->
            nodes.singleOrNull { it.project == test.project && it.variant == test.variant && it.kind == Kind.MAIN && it.usage == test.usage }
                ?.let { edges += Edge(test, it, "<tested-output>") }
        }
        return Snapshot(modules, nodes.sortedBy { it.key }, edges.distinct().sortedWith(compareBy({ it.from.key }, { it.to.key }, { it.configuration })),
            sources.distinct().sortedWith(compareBy({ it.project }, { it.sourceSet }, { it.directory })), problems.distinctBy { it.key })
    }

    private fun context(path: String, config: Configuration, android: Boolean): Context? {
        if (!config.isCanBeResolved) return null
        val usage = when {
            config.name.endsWith("CompileClasspath") || config.name == "compileClasspath" -> Usage.COMPILE
            config.name.endsWith("RuntimeClasspath") || config.name == "runtimeClasspath" -> Usage.RUNTIME
            else -> return null
        }
        val prefix = config.name.removeSuffix(if (usage == Usage.COMPILE) "CompileClasspath" else "RuntimeClasspath")
            .let { if (it == "compileClasspath" || it == "runtimeClasspath") "" else it }
        val (variant, kind) = if (android) when {
            prefix.endsWith("AndroidTest") -> prefix.removeSuffix("AndroidTest") to Kind.ANDROID_TEST
            prefix.endsWith("UnitTest") -> prefix.removeSuffix("UnitTest") to Kind.UNIT_TEST
            prefix.endsWith("TestFixtures") -> prefix.removeSuffix("TestFixtures") to Kind.TEST_FIXTURES
            else -> prefix to Kind.MAIN
        } else when (prefix) {
            "" -> "main" to Kind.MAIN
            "test" -> "main" to Kind.UNIT_TEST
            "testFixtures" -> "main" to Kind.TEST_FIXTURES
            else -> return null
        }
        if (variant.isEmpty()) return null
        val buildType = config.attributes.keySet().firstOrNull { it.name == "com.android.build.api.attributes.BuildTypeAttr" }
            ?.let { config.attributes.getAttribute(it).toString() }
        return Context(config, Node(path, variant, kind, usage), buildType)
    }

    private fun selectTarget(dep: ProjectDependency, targetProject: Project, consumer: Context, candidates: List<Context>): Node? {
        if (dep.artifacts.isNotEmpty() || dep.attributes.keySet().any { it.name != "com.android.build.api.attributes.BuildTypeAttr" }) return null
        val fixture = dep.requestedCapabilities.any { it.name.endsWith("-test-fixtures") }
        if (dep.requestedCapabilities.isNotEmpty() && (!fixture || dep.requestedCapabilities.size != 1)) return null
        val kind = if (fixture) Kind.TEST_FIXTURES else Kind.MAIN
        val available = candidates.map { it.node }.filter { it.kind == kind && it.usage == consumer.node.usage }.distinct()
        val explicit = dep.targetConfiguration
        if (explicit != null && explicit != "default") {
            val configuration = targetProject.configurations.findByName(explicit) ?: return null
            if (!configuration.isCanBeConsumed) return null
            val suffix = when {
                explicit == "apiElements" -> "apiElements"
                explicit == "runtimeElements" -> "runtimeElements"
                explicit.endsWith("ApiElements") -> "ApiElements"
                explicit.endsWith("RuntimeElements") -> "RuntimeElements"
                else -> return null
            }
            val prefix = explicit.removeSuffix(suffix)
            val targetKind = if (prefix.endsWith("TestFixtures") || prefix == "testFixtures") Kind.TEST_FIXTURES else Kind.MAIN
            val variant = prefix.removeSuffix("TestFixtures").let { if (it.isEmpty() || it == "testFixtures") "main" else it }
            val usage = if (suffix.equals("ApiElements", ignoreCase = true)) Usage.COMPILE else Usage.RUNTIME
            return candidates.map { it.node }.singleOrNull { it.kind == targetKind && it.variant == variant && it.usage == usage }
        }
        val requestedBuildType = dep.attributes.keySet().firstOrNull { it.name == "com.android.build.api.attributes.BuildTypeAttr" }
            ?.let { dep.attributes.getAttribute(it).toString() }
        val variant = requestedBuildType ?: consumer.node.variant
        // Never guess among Android flavors or use a fallback variant silently.
        return available.singleOrNull { it.variant == variant }
            ?: available.singleOrNull { it.variant == "main" }
            ?: if (requestedBuildType == null) available.singleOrNull { it.variant == consumer.buildType } else null
    }

    private fun unsupported(project: String, config: String, reason: String) = Finding("UNSUPPORTED_CONTEXT", reason,
        "$project:$config", "Attach the declaration to a supported classpath or add an explicit tested context mapping; do not skip it.")
}
