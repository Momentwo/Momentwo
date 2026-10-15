package cord.eoeo.momentwo.convention.architecture

import org.junit.Assert.*
import org.junit.Test

class ArchitectureChecksTest {
    private fun node(path: String, variant: String = "debug", kind: Kind = Kind.MAIN, usage: Usage = Usage.COMPILE) = Node(path, variant, kind, usage)
    private fun model(vararg edges: Edge): Snapshot {
        val nodes = edges.flatMap { listOf(it.from, it.to) }.distinct()
        return Snapshot(nodes.map { Module(it.project) }.distinct(), nodes, edges.toList())
    }
    private fun edge(a: String, b: String) = Edge(node(a), node(b), "implementation")

    @Test fun existingForbiddenDirectionsAreRejected() {
        val examples = listOf(
            Triple(":core:data", ":feature:album:api", "CORE_TO_FEATURE_OR_APP"),
            Triple(":core:common", ":app", "CORE_TO_FEATURE_OR_APP"),
            Triple(":feature:album:impl", ":app", "FEATURE_TO_APP"),
            Triple(":feature:album:impl", ":feature:photo:impl", "CROSS_FEATURE_IMPL"),
            Triple(":feature:album:api", ":feature:album:impl", "API_TO_IMPLEMENTATION"),
            Triple(":feature:album:api", ":core:data", "API_TO_IMPLEMENTATION"),
            Triple(":app", ":core:data", "APP_COMPOSITION"),
            Triple(":core:common", ":core:testing", "TESTING_IN_PRODUCTION"),
        )
        examples.forEach { (from, to, rule) ->
            assertEquals("$from -> $to", rule, BoundaryPolicy.violation(Module(from), Module(to), Kind.MAIN))
        }
    }

    @Test fun normalApisCoreAndIntegrationTestDependenciesAreAllowed() {
        listOf(":feature:album:api", ":feature:photo:api", ":core:data", ":core:domain").forEach {
            assertNull(BoundaryPolicy.violation(Module(":feature:album:impl"), Module(it), Kind.MAIN))
        }
        assertNull(BoundaryPolicy.violation(Module(":app"), Module(":core:data"), Kind.ANDROID_TEST))
        assertNull(BoundaryPolicy.violation(Module(":core:common"), Module(":core:testing"), Kind.UNIT_TEST))
        assertNull(BoundaryPolicy.violation(Module(":feature:auth:impl"), Module(":core:datastore"), Kind.UNIT_TEST))
        assertEquals("CROSS_FEATURE_IMPL", BoundaryPolicy.violation(Module(":feature:album:impl"), Module(":feature:photo:impl"), Kind.UNIT_TEST))
    }

    @Test fun apiCannotReachDataThroughOtherwiseAllowedCoreModules() {
        val findings = ArchitectureChecks.boundaries(model(edge(":feature:album:api", ":core:domain"), edge(":core:domain", ":core:data")))
        val transitive = findings.single { it.rule == "API_TRANSITIVE_IMPLEMENTATION" }
        assertTrue(transitive.message.contains(":core:domain"))
        assertTrue(transitive.message.contains(":core:data"))
    }

    @Test fun selfReferenceAndThreeModuleCycleAreReportedWithAllMembers() {
        val edges = arrayOf(edge(":core:self", ":core:self"), edge(":core:a", ":core:b"), edge(":core:b", ":core:c"), edge(":core:c", ":core:a"))
        val findings = ArchitectureChecks.cycles(model(*edges))
        assertEquals(2, findings.size)
        assertTrue(findings.any { it.message.contains(":core:self") })
        assertTrue(findings.any { it.message.contains(":core:a") && it.message.contains(":core:b") && it.message.contains(":core:c") })
        assertTrue(findings.all { it.message.contains("--implementation-->") })
    }

    @Test fun separateTwoNodeCyclesAndDuplicateEdgesAreStable() {
        val a = edge(":core:a", ":core:b")
        val snapshot = model(a, a, edge(":core:b", ":core:a"), edge(":core:c", ":core:d"), edge(":core:d", ":core:c"))
        assertEquals(2, ArchitectureChecks.cycles(snapshot).size)
        assertEquals(ArchitectureChecks.cycles(snapshot), ArchitectureChecks.cycles(snapshot.copy(edges = snapshot.edges.reversed(), nodes = snapshot.nodes.reversed())))
    }

    @Test fun testsPointToProductionOutputsWithoutCreatingFalseCycle() {
        val test = node(":core:b", kind = Kind.UNIT_TEST)
        assertTrue(ArchitectureChecks.cycles(model(edge(":core:a", ":core:b"), Edge(test, node(":core:a"), "testImplementation"),
            Edge(test, node(":core:b"), "<tested-output>"))).isEmpty())
    }

    @Test fun oppositeEdgesInDifferentVariantsOrUsagesDoNotFormCycle() {
        val snapshot = model(edge(":core:a", ":core:b"),
            Edge(node(":core:b", "release"), node(":core:a", "release"), "releaseImplementation"),
            Edge(node(":core:b", usage = Usage.RUNTIME), node(":core:a", usage = Usage.RUNTIME), "runtimeOnly"))
        assertTrue(ArchitectureChecks.cycles(snapshot).isEmpty())
    }

    @Test fun fixtureOutputCycleIsDetectedInItsOwnContext() {
        val a = node(":core:a", kind = Kind.TEST_FIXTURES)
        val b = node(":core:b", kind = Kind.TEST_FIXTURES)
        assertEquals(1, ArchitectureChecks.cycles(model(Edge(a, b, "testFixturesImplementation"), Edge(b, a, "testFixturesImplementation"))).size)
    }

    @Test fun unknownRolesAndUnsupportedContextsNeverPassSilently() {
        val snapshot = model(edge(":unknown:module", ":core:common"))
        assertTrue(ArchitectureChecks.boundaries(snapshot).any { it.rule == "UNKNOWN_MODULE" })
        val issue = Finding("UNSUPPORTED_CONTEXT", "custom", ":core:a:custom", "Map it")
        assertEquals(listOf(issue), ArchitectureChecks.cycles(snapshot.copy(problems = listOf(issue))))
    }
}
