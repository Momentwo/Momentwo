package cord.eoeo.momentwo.convention.architecture

import java.io.File
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class SourceBoundaryChecksTest {
    @get:Rule val folder = TemporaryFolder()
    private val a = Module(":feature:album:impl", "fixture.album.impl", true)
    private val b = Module(":feature:album-detail:impl", "fixture.albumdetail.impl", true)
    private fun check(code: String, sourceSet: String = "main"): List<Finding> {
        val dir = folder.newFolder()
        val file = File(dir, "Source.kt").apply { writeText(code) }
        val snapshot = Snapshot(listOf(a, b), listOf(Node(a.path, "debug", Kind.MAIN, Usage.COMPILE)), emptyList(),
            listOf(SourceRoot(a.path, sourceSet, dir.path)))
        return SourceBoundaryChecks.check(snapshot, setOf(file), folder.root)
    }

    @Test fun aliasesWildcardsAndJavaStaticImportsReportExactLineAndOwner() {
        val findings = check("package fixture.album.impl\n\nimport fixture.albumdetail.impl.Screen as Detail\nimport fixture.albumdetail.impl.*\nimport static fixture.albumdetail.impl.Tools.call;\n")
        assertEquals(3, findings.size)
        assertTrue(findings.all { it.rule == "CROSS_FEATURE_IMPL" && it.message.contains(b.path) })
        assertEquals(listOf(3, 4, 5), findings.map { Regex(":(\\d+) ").find(it.location)!!.groupValues[1].toInt() }.sorted())
    }

    @Test fun javaImportsOnThePackageLineAreStillChecked() {
        val finding = check("package fixture.album.impl; import static fixture.albumdetail.impl.Tools.call;\n").single()
        assertEquals("CROSS_FEATURE_IMPL", finding.rule)
        assertTrue(finding.location.contains(":1 (main)"))
    }

    @Test fun nestedCommentsRawStringsAndEscapedStringsAreIgnored() {
        val code = "package fixture.album.impl\n/* outer /* nested */\nimport fixture.albumdetail.impl.Bad\n*/\n" +
            "val text = \"\"\"\nimport fixture.albumdetail.impl.Bad\n\"\"\"\n" +
            "// import fixture.albumdetail.impl.Bad\nval other = \"import fixture.albumdetail.impl.Bad\\\"\"\n" +
            "@SerialName(\"core.navigation.MomentwoDestination\")\n"
        assertTrue(check(code).isEmpty())
        assertEquals(code.count { it == '\n' }, SourceBoundaryChecks.stripCommentsAndStrings(code).count { it == '\n' })
    }

    @Test fun ownAndExternalImportsAreAllowedButTestCannotAccessOtherImpl() {
        assertTrue(check("import fixture.album.impl.Screen\nimport kotlin.collections.List\n").isEmpty())
        assertEquals("CROSS_FEATURE_IMPL", check("import fixture.albumdetail.impl.Screen", "testDebug").single().rule)
    }

    @Test fun jvmPackageDeclarationsProvideOwnershipWithoutAndroidNamespace() {
        val dirA = folder.newFolder(); val dirB = folder.newFolder()
        val fileA = File(dirA, "A.java").apply { writeText("package fixture.core;\nimport fixture.feature.api.Destination;\n") }
        val fileB = File(dirB, "B.kt").apply { writeText("package fixture.feature.api\nclass Destination") }
        val snapshot = Snapshot(listOf(Module(":core:model"), Module(":feature:album:api")), emptyList(), emptyList(),
            listOf(SourceRoot(":core:model", "main", dirA.path), SourceRoot(":feature:album:api", "main", dirB.path)))
        assertEquals("CORE_TO_FEATURE_OR_APP", SourceBoundaryChecks.check(snapshot, setOf(fileA, fileB), folder.root).single().rule)
    }

    @Test fun unmappedSourceSetCannotHideImports() {
        assertEquals("UNSUPPORTED_SOURCE_SET", check("package fixture.album.impl\n", "custom").single().rule)
    }

    @Test fun duplicatePackageCannotBeAssumedToBelongToTheImportingModule() {
        val dir = folder.newFolder()
        val file = File(dir, "Source.kt").apply { writeText("package fixture.shared\nimport fixture.shared.Other\n") }
        val snapshot = Snapshot(listOf(a.copy(namespace = "fixture.shared"), b.copy(namespace = "fixture.shared")),
            emptyList(), emptyList(), listOf(SourceRoot(a.path, "main", dir.path)))
        assertEquals("AMBIGUOUS_PACKAGE", SourceBoundaryChecks.check(snapshot, setOf(file), folder.root).single().rule)
    }
}
