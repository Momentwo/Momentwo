package cord.eoeo.momentwo.convention.architecture

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFile
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.*

internal abstract class ArchitectureTask : DefaultTask() {
    @get:Input abstract val snapshot: Property<Snapshot>
    @get:Input abstract val mode: Property<String>
    @get:InputFiles @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val sourceFiles: ConfigurableFileCollection
    @get:Internal abstract val reportDirectory: DirectoryProperty
    @get:Internal abstract val rootDirectory: DirectoryProperty
    @get:OutputFile val jsonReport: Provider<RegularFile> get() = reportDirectory.file(mode.map { "$it.json" })
    @get:OutputFile val textReport: Provider<RegularFile> get() = reportDirectory.file(mode.map { "$it.txt" })

    init { group = "verification" }

    @TaskAction fun checkArchitecture() {
        val model = snapshot.get()
        val findings = buildList {
            if (mode.get() != "boundaries") addAll(ArchitectureChecks.cycles(model))
            if (mode.get() != "cycles") {
                addAll(ArchitectureChecks.boundaries(model))
                addAll(SourceBoundaryChecks.check(model, sourceFiles.files, rootDirectory.get().asFile))
            }
        }.distinctBy { it.key }.sortedBy { it.key }
        val summary = "${mode.get()}: ${model.modules.size} modules, ${model.nodes.size} contexts, ${model.edges.size} edges, ${findings.size} violations"
        val report = summary + "\n" + findings.joinToString("\n\n") { "[${it.rule}] ${it.location}\n${it.message}\nFix: ${it.fix}" } + "\n"
        write(textReport.get().asFile, report)
        val json = """{"check":${quote(mode.get())},"modules":${model.modules.size},"contexts":${model.nodes.size},"edges":${model.edges.size},"violations":[""" +
            findings.joinToString(",") { """{"rule":${quote(it.rule)},"location":${quote(it.location)},"message":${quote(it.message)},"fix":${quote(it.fix)}}""" } + "]}\n"
        write(jsonReport.get().asFile, json)
        if (findings.isNotEmpty()) throw GradleException(report + "Report: ${textReport.get().asFile}")
        logger.lifecycle(summary)
    }

    private fun write(file: File, text: String) { file.parentFile.mkdirs(); file.writeText(text) }
    private fun quote(value: String): String = "\"" + buildString {
        value.forEach { char -> append(when (char) {
            '"' -> "\\\""
            '\\' -> "\\\\"
            '\n' -> "\\n"
            '\r' -> "\\r"
            '\t' -> "\\t"
            else -> if (char.code < 32) "\\u%04x".format(char.code) else char.toString()
        }) }
    } + "\""
}
