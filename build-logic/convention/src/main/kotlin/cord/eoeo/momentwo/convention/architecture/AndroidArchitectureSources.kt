package cord.eoeo.momentwo.convention.architecture

import org.gradle.api.Project

/** Read public AGP DSL getters across plugin classloader scopes (including TestKit injection). */
internal object AndroidArchitectureSources {
    private fun Any.read(name: String): Any? = javaClass.getMethod(name).invoke(this)
    fun namespace(project: Project): String = project.extensions.getByName("android").read("getNamespace") as? String ?: ""
    fun roots(project: Project): List<SourceRoot> {
        val sourceSets = project.extensions.getByName("android").read("getSourceSets") as Iterable<*>
        return sourceSets.flatMap { source ->
            requireNotNull(source)
            val name = source.read("getName") as String
            listOf("getJava", "getKotlin").flatMap { getter ->
                val directorySet = requireNotNull(source.read(getter))
                (directorySet.read("getDirectories") as Set<*>).map {
                    SourceRoot(project.path, name, project.file(requireNotNull(it)).absolutePath)
                }
            }
        }
    }
}
