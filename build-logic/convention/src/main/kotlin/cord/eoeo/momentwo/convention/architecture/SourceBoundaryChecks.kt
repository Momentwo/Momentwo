package cord.eoeo.momentwo.convention.architecture

import java.io.File

internal object SourceBoundaryChecks {
    private val packagePattern = Regex("\\bpackage\\s+([\\w.`]+)")
    private val importPattern = Regex("\\bimport\\s+(?:static\\s+)?([\\w`$]+(?:\\s*\\.\\s*[\\w`$*]+)*)")

    fun check(snapshot: Snapshot, files: Set<File>, root: File): List<Finding> {
        data class Source(val file: File, val owner: SourceRoot, val code: String)
        val sources = files.sortedBy { it.path }.mapNotNull { file ->
            val owner = snapshot.sources.filter { file.toPath().startsWith(File(it.directory).toPath()) }
                .maxByOrNull { it.directory.length } ?: return@mapNotNull null
            Source(file, owner, stripCommentsAndStrings(file.readText()))
        }
        val modules = snapshot.modules.associateBy { it.path }
        val packages = mutableMapOf<String, MutableSet<String>>()
        snapshot.modules.filter { it.namespace.isNotEmpty() }.forEach {
            packages.getOrPut(it.namespace) { mutableSetOf() }.add(it.path)
        }
        sources.forEach { source ->
            packagePattern.find(source.code)?.groupValues?.get(1)?.replace("`", "")?.let {
                packages.getOrPut(it) { mutableSetOf() }.add(source.owner.project)
            }
        }
        val findings = mutableListOf<Finding>()
        sources.forEach { source ->
            val from = modules.getValue(source.owner.project)
            val kind = sourceKind(source.owner.sourceSet, snapshot.nodes.filter { it.project == from.path })
            val location = source.file.relativeTo(root).invariantSeparatorsPath
            if (kind == null) {
                findings += Finding("UNSUPPORTED_SOURCE_SET", source.owner.sourceSet, location,
                    "Map this source set to its production/unit/Android test context before using it.")
                return@forEach
            }
            importPattern.findAll(source.code).forEach imports@{ match ->
                val name = match.groupValues[1].replace(Regex("\\s+|`"), "")
                val prefix = packages.keys.filter { name == it || name.startsWith("$it.") }.maxByOrNull { it.length }
                    ?: return@imports // External imports and fully-qualified references are outside this check.
                val owners = packages.getValue(prefix)
                val line = source.code.substring(0, source.code.indexOf("import", match.range.first)).count { it == '\n' } + 1
                val at = "$location:$line (${source.owner.sourceSet})"
                if (owners.size != 1) {
                    findings += Finding("AMBIGUOUS_PACKAGE", "$name belongs to ${owners.sorted()}", at,
                        "Give modules distinct namespaces/packages or add an explicit ownership rule.")
                } else {
                    if (from.path in owners) return@imports
                    val to = modules.getValue(owners.single())
                    BoundaryPolicy.violation(from, to, kind)?.let { rule ->
                        findings += Finding(rule, "${from.path} imports $name from ${to.path}", at, BoundaryPolicy.fix(rule))
                    }
                }
            }
        }
        return findings.distinctBy { it.key }.sortedBy { it.key }
    }

    private fun sourceKind(name: String, nodes: List<Node>): Kind? = when {
        name == "main" -> Kind.MAIN
        name.startsWith("androidTest") -> Kind.ANDROID_TEST
        name.startsWith("testFixtures") -> Kind.TEST_FIXTURES
        name.startsWith("test") -> Kind.UNIT_TEST
        nodes.any { it.kind == Kind.MAIN && (it.variant.equals(name, ignoreCase = true) || it.variant.endsWith(name.replaceFirstChar(Char::uppercase))) } -> Kind.MAIN
        else -> null
    }

    /** Preserve offsets/newlines, including nested Kotlin comments and raw strings. */
    internal fun stripCommentsAndStrings(input: String): String {
        val out = input.toCharArray()
        var i = 0
        fun blank(end: Int) { while (i < end) { if (out[i] != '\n' && out[i] != '\r') out[i] = ' '; i++ } }
        while (i < input.length) {
            when {
                input.startsWith("//", i) -> {
                    val end = input.indexOf('\n', i).let { if (it < 0) input.length else it }; blank(end)
                }
                input.startsWith("/*", i) -> {
                    var end = i + 2; var depth = 1
                    while (end < input.length && depth > 0) {
                        when {
                            input.startsWith("/*", end) -> { depth++; end += 2 }
                            input.startsWith("*/", end) -> { depth--; end += 2 }
                            else -> end++
                        }
                    }
                    blank(end)
                }
                input.startsWith("\"\"\"", i) -> {
                    val end = input.indexOf("\"\"\"", i + 3).let { if (it < 0) input.length else it + 3 }; blank(end)
                }
                input[i] == '"' || input[i] == '\'' -> {
                    val quote = input[i]; var end = i + 1
                    while (end < input.length) {
                        if (input[end] == '\\') end = minOf(end + 2, input.length)
                        else if (input[end++] == quote) break
                    }
                    blank(end)
                }
                else -> i++
            }
        }
        return String(out)
    }
}
