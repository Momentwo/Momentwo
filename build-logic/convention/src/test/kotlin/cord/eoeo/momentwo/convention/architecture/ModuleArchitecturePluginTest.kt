package cord.eoeo.momentwo.convention.architecture

import java.io.File
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class ModuleArchitecturePluginTest {
    @get:Rule val temporary = TemporaryFolder()

    private fun fixture(vararg modules: String): File = temporary.newFolder().apply {
        resolve("settings.gradle").writeText("rootProject.name='fixture'\n" + modules.joinToString("\n") { "include '$it'" })
        resolve("build.gradle").writeText("plugins { id 'momentwo.module-architecture' }\n")
        resolve("gradle.properties").writeText("org.gradle.jvmargs=-Xmx512m\norg.gradle.workers.max=2\n")
        modules.forEach { path ->
            resolve(path.trim(':').replace(':', '/')).apply { mkdirs(); resolve("build.gradle").writeText("plugins { id 'java-library' }\n") }
        }
    }
    private fun File.build(path: String): File = resolve(path.trim(':').replace(':', '/') + "/build.gradle")
    private fun runner(dir: File, vararg tasks: String) = GradleRunner.create().withProjectDir(dir).withPluginClasspath()
        .withArguments(*tasks, "--stacktrace", "--console=plain", "--max-workers=2")

    @Test fun normalGraphAndTestBackReferencePassWithoutCompilingOrResolvingArtifacts() {
        val dir = fixture(":core:a", ":core:b", ":core:testing")
        dir.build(":core:a").appendText("dependencies { implementation project(':core:b') }\n")
        dir.build(":core:b").appendText("dependencies { testImplementation project(':core:a'); testImplementation project(':core:testing'); implementation 'unresolvable:missing:1' }\n")
        val result = runner(dir, "checkArchitecture", "--offline").build()
        assertEquals(TaskOutcome.SUCCESS, result.task(":checkArchitecture")!!.outcome)
        assertFalse(result.tasks.any { it.path.endsWith("compileJava") })
        assertTrue(dir.resolve("build/reports/module-architecture/architecture.json").readText().contains("\"violations\":[]"))
        assertTrue(dir.resolve("build/reports/module-architecture/architecture.txt").readText().contains("3 modules"))
    }

    @Test fun conventionAndLateInheritedCompileOnlyAndRuntimeOnlyDeclarationsAreInspected() {
        val dir = fixture(":core:a", ":feature:album:impl")
        dir.resolve("convention.gradle").writeText("dependencies { compileOnly project(':feature:album:impl') }\n")
        dir.build(":core:a").appendText("""
            apply from: rootProject.file('convention.gradle')
            configurations { bridge }
            configurations.runtimeOnly.extendsFrom(configurations.bridge)
            afterEvaluate { dependencies { bridge project(':feature:album:impl') } }
        """.trimIndent())
        val result = runner(dir, "checkModuleBoundaries").buildAndFail()
        assertTrue(result.output.contains("CORE_TO_FEATURE_OR_APP"))
        assertTrue(result.output.contains("compileOnly"))
        assertTrue(result.output.contains("bridge"))
        assertTrue(result.output.contains("RUNTIME"))
    }

    @Test fun cycleTaskRunsIndependentlyEvenWhenCompileTaskGraphWouldBeCircular() {
        val dir = fixture(":core:a", ":core:b", ":core:c")
        dir.build(":core:a").appendText("dependencies { api project(':core:b') }\n")
        dir.build(":core:b").appendText("dependencies { implementation project(':core:c') }\n")
        dir.build(":core:c").appendText("dependencies { runtimeOnly project(':core:a') }\n")
        val result = runner(dir, "checkModuleCycles").buildAndFail()
        assertTrue(result.output.contains("DEPENDENCY_CYCLE"))
        assertTrue(result.output.contains("runtimeOnly"))
        assertTrue(result.output.contains(":core:a[main/MAIN/RUNTIME]"))
        assertFalse(result.tasks.any { it.path.contains("compileJava") })
    }

    @Test fun everyModuleCheckAndRootCheckInvokeArchitectureGate() {
        val dir = fixture(":core:a", ":feature:album:impl")
        dir.build(":core:a").appendText("dependencies { implementation project(':feature:album:impl') }\n")
        assertTrue(runner(dir, ":core:a:check").buildAndFail().output.contains("CORE_TO_FEATURE_OR_APP"))
        assertTrue(runner(dir, ":check").buildAndFail().output.contains("CORE_TO_FEATURE_OR_APP"))
    }

    @Test fun configurationOnDemandCannotProduceAnIncompleteGreenGraph() {
        val dir = fixture(":core:a", ":feature:album:impl")
        dir.build(":core:a").appendText("dependencies { implementation project(':feature:album:impl') }\n")
        val failure = runner(dir, "checkArchitecture", "--configure-on-demand").buildAndFail()
        assertTrue(failure.output.contains("cannot run with --configure-on-demand"))
    }

    @Test fun unknownConfigurationAndUnsupportedOutputFailWithActionableMessage() {
        val dir = fixture(":core:a", ":core:b")
        dir.build(":core:a").appendText("configurations { hidden }; dependencies { hidden project(':core:b') }\n")
        var result = runner(dir, "checkArchitecture", "--continue").buildAndFail()
        assertTrue(result.output.contains("UNSUPPORTED_CONTEXT"))
        assertTrue(result.output.contains(":core:a:hidden"))
        dir.build(":core:a").writeText("plugins { id 'java-library' }; dependencies { implementation project(path: ':core:b', configuration: 'testImplementation') }")
        result = runner(dir, "checkModuleCycles").buildAndFail()
        assertTrue(result.output.contains("targetConfiguration=testImplementation"))
    }

    @Test fun unknownRequestedAttributesCannotSilentlySelectTheWrongVariant() {
        val dir = fixture(":core:a", ":core:b")
        dir.build(":core:a").appendText("""
            dependencies {
                implementation(project(':core:b')) {
                    attributes { attribute(org.gradle.api.attributes.Attribute.of('fixture.unmapped', String), 'other') }
                }
            }
        """.trimIndent())
        val result = runner(dir, "checkModuleCycles").buildAndFail()
        assertTrue(result.output.contains("UNSUPPORTED_CONTEXT"))
        assertTrue(result.output.contains("fixture.unmapped"))
    }

    @Test fun moduleAdditionAndRemovalNeedNoCountChangeAndSourceInputsInvalidateReports() {
        val dir = fixture(":core:a", ":feature:album:api")
        val source = dir.resolve("core/a/src/main/java/Fixture.java").apply { parentFile.mkdirs(); writeText("package fixture.core;\n") }
        dir.resolve("feature/album/api/src/main/java/Destination.java").apply { parentFile.mkdirs(); writeText("package fixture.api;\n") }
        runner(dir, "checkArchitecture").build()
        source.appendText("\nimport fixture.api.Destination;\n")
        val result = runner(dir, "checkModuleBoundaries").buildAndFail()
        assertTrue(result.output.contains("Fixture.java:3"))
        source.writeText("package fixture.core;\n")
        dir.resolve("settings.gradle").appendText("\ninclude ':core:added'\n")
        dir.resolve("core/added/build.gradle").apply { parentFile.mkdirs(); writeText("plugins { id 'java-library' }") }
        runner(dir, "checkArchitecture").build()
        assertTrue(dir.resolve("build/reports/module-architecture/architecture.txt").readText().contains("3 modules"))
        dir.resolve("settings.gradle").writeText("include ':core:a'\n")
        runner(dir, "checkArchitecture").build()
        assertTrue(dir.resolve("build/reports/module-architecture/architecture.txt").readText().contains("1 modules"))

    }

    @Test fun aggregateCollectsBothBoundaryAndCycleFailuresBeforeThrowing() {
        val dir = fixture(":core:a", ":feature:album:impl")
        dir.build(":core:a").appendText("dependencies { implementation project(':feature:album:impl') }\n")
        dir.build(":feature:album:impl").appendText("dependencies { implementation project(':core:a') }\n")
        val result = runner(dir, "checkArchitecture").buildAndFail()
        assertTrue(result.output.contains("CORE_TO_FEATURE_OR_APP"))
        assertTrue(result.output.contains("DEPENDENCY_CYCLE"))
        val report = dir.resolve("build/reports/module-architecture/architecture.json").readText()
        assertTrue(report.contains("CORE_TO_FEATURE_OR_APP"))
        assertTrue(report.contains("DEPENDENCY_CYCLE"))
    }

    @Test fun generatedSourceFilesAreExcludedAndCustomSourceDirectoriesAreChecked() {
        val dir = fixture(":core:a", ":feature:album:impl")
        dir.build(":core:a").appendText("sourceSets.main.java.srcDir('custom')\n")
        dir.resolve("feature/album/impl/src/main/java/Feature.java").apply { parentFile.mkdirs(); writeText("package fixture.impl;\n") }
        dir.resolve("core/a/custom/generated/Generated.java").apply { parentFile.mkdirs(); writeText("import fixture.impl.Feature;\n") }
        dir.resolve("core/a/src/main/resources/Example.kt").apply { parentFile.mkdirs(); writeText("import fixture.impl.Feature;\n") }
        runner(dir, "checkModuleBoundaries").build()
        dir.resolve("core/a/custom/Manual.java").writeText("package fixture.core;\nimport fixture.impl.Feature;\n")
        assertTrue(runner(dir, "checkModuleBoundaries").buildAndFail().output.contains("custom/Manual.java:2"))
    }

    @Test fun explicitFixtureOutputIsMappedAndRealFixtureCycleIsDetected() {
        val dir = fixture(":core:a", ":core:b")
        listOf(":core:a", ":core:b").forEach { dir.build(it).appendText("apply plugin: 'java-test-fixtures'\n") }
        dir.build(":core:a").appendText("dependencies { testFixturesImplementation project(path: ':core:b', configuration: 'testFixturesApiElements') }\n")
        dir.build(":core:b").appendText("dependencies { testFixturesImplementation project(path: ':core:a', configuration: 'testFixturesApiElements') }\n")
        val result = runner(dir, "checkModuleCycles").buildAndFail()
        assertTrue(result.output.contains("DEPENDENCY_CYCLE"))
        assertTrue(result.output.contains("TEST_FIXTURES"))
    }

    @Test fun capabilitySelectedFixturesDoNotGetMistakenForProductionOutputs() {
        val dir = fixture(":core:a", ":core:b")
        listOf(":core:a", ":core:b").forEach { dir.build(it).appendText("apply plugin: 'java-test-fixtures'\n") }
        dir.build(":core:a").appendText("dependencies { testFixturesImplementation testFixtures(project(':core:b')) }\n")
        dir.build(":core:b").appendText("dependencies { testFixturesImplementation testFixtures(project(':core:a')) }\n")
        val result = runner(dir, "checkModuleCycles").buildAndFail()
        assertTrue(result.output.contains("DEPENDENCY_CYCLE"))
        assertTrue(result.output.contains("TEST_FIXTURES"))
        assertFalse(result.output.contains("UNSUPPORTED_CONTEXT"))
    }

    @Test fun explicitStandardJavaOutputsPreserveSelectedCompileOrRuntimeContext() {
        val dir = fixture(":core:a", ":core:b")
        dir.build(":core:a").appendText("dependencies { compileOnly project(path: ':core:b', configuration: 'apiElements'); runtimeOnly project(path: ':core:b', configuration: 'runtimeElements') }\n")
        runner(dir, "checkArchitecture").build()
        dir.build(":core:b").appendText("dependencies { runtimeOnly project(path: ':core:a', configuration: 'runtimeElements') }\n")
        val result = runner(dir, "checkModuleCycles").buildAndFail()
        assertTrue(result.output.contains("DEPENDENCY_CYCLE"))
        assertTrue(result.output.contains("RUNTIME"))
        assertFalse(result.output.contains("UNSUPPORTED_CONTEXT"))
    }

    @Test fun checkerCanReuseConfigurationCacheAndStillSeesNewSourceFiles() {
        val dir = fixture(":core:a", ":feature:album:impl")
        val sources = dir.resolve("core/a/src/main/java").apply { mkdirs() }
        dir.resolve("feature/album/impl/src/main/java/Feature.java").apply { parentFile.mkdirs(); writeText("package fixture.impl;\n") }
        runner(dir, "checkArchitecture", "--configuration-cache").build()
        assertTrue(runner(dir, "checkArchitecture", "--configuration-cache").build().output.contains("Reusing configuration cache"))
        sources.resolve("Added.java").writeText("package fixture.core;\nimport fixture.impl.Feature;\n")
        val failure = runner(dir, "checkArchitecture", "--configuration-cache").buildAndFail()
        assertTrue(failure.output.contains("CORE_TO_FEATURE_OR_APP"))
        assertTrue(failure.output.contains("Added.java:2"))
    }

    @Test fun androidVariantsAreSeparateAndTestsConsumeOnlyProductionOutput() {
        val sdk = System.getenv("ANDROID_HOME") ?: System.getenv("ANDROID_SDK_ROOT")
            ?: error("Set ANDROID_HOME to run the AGP 8.5.2 TestKit fixture")
        val dir = fixture(":core:a", ":core:b")
        dir.resolve("settings.gradle").writeText("pluginManagement { repositories { google(); mavenCentral(); gradlePluginPortal() } }; include ':core:a', ':core:b'")
        dir.resolve("build.gradle").writeText("plugins { id 'com.android.library' version '8.5.2' apply false; id 'momentwo.module-architecture' }")
        dir.resolve("local.properties").writeText("sdk.dir=${sdk.replace("\\", "\\\\")}\n")
        listOf("a", "b").forEach { name -> dir.build(":core:$name").writeText("plugins { id 'com.android.library' }; android { namespace 'fixture.core.$name'; compileSdk 34 }\n") }
        dir.build(":core:a").appendText("dependencies { debugImplementation project(':core:b') }\n")
        dir.build(":core:b").appendText("dependencies { releaseImplementation project(':core:a'); testDebugImplementation project(':core:a'); androidTestImplementation project(':core:a') }\n")
        runner(dir, "checkArchitecture").build()
        dir.build(":core:b").appendText("dependencies { debugImplementation project(':core:a') }\n")
        val failure = runner(dir, "checkModuleCycles").buildAndFail()
        assertTrue(failure.output.contains("DEPENDENCY_CYCLE"))
        assertTrue(failure.output.contains("debug/MAIN"))
        dir.resolve("settings.gradle").appendText("\ninclude ':feature:album:impl'\n")
        dir.resolve("feature/album/impl/build.gradle").apply { parentFile.mkdirs(); writeText("plugins { id 'java-library' }") }
        dir.resolve("feature/album/impl/src/main/java/Screen.java").apply {
            parentFile.mkdirs(); writeText("package fixture.feature.impl;\n")
        }
        dir.resolve("core/a/src/main/kotlin/Manual.kt").apply {
            parentFile.mkdirs(); writeText("package fixture.core.a\nimport fixture.feature.impl.Screen\n")
        }
        val imports = runner(dir, "checkModuleBoundaries").buildAndFail()
        assertTrue(imports.output.contains("CORE_TO_FEATURE_OR_APP"))
        assertTrue(imports.output.contains("Manual.kt:2"))
    }
}
