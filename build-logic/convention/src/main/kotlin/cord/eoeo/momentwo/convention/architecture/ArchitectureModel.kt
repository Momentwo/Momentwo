package cord.eoeo.momentwo.convention.architecture

import java.io.Serializable

internal enum class Role { APP, CORE, API, IMPL, UNKNOWN }
internal enum class Kind { MAIN, UNIT_TEST, ANDROID_TEST, TEST_FIXTURES }
internal enum class Usage { COMPILE, RUNTIME }

internal data class Module(val path: String, val namespace: String = "", val android: Boolean = false) : Serializable {
    val role: Role get() = when {
        path == ":app" -> Role.APP
        Regex(":core:[^:]+").matches(path) -> Role.CORE
        Regex(":feature:[^:]+:api").matches(path) -> Role.API
        Regex(":feature:[^:]+:impl").matches(path) -> Role.IMPL
        else -> Role.UNKNOWN
    }
}

internal data class Node(
    val project: String, val variant: String, val kind: Kind, val usage: Usage,
) : Serializable {
    val key: String get() = "$project[$variant/${kind.name}/${usage.name}]"
}

internal data class Edge(val from: Node, val to: Node, val configuration: String) : Serializable
internal data class SourceRoot(val project: String, val sourceSet: String, val directory: String) : Serializable
internal data class Finding(
    val rule: String, val message: String, val location: String, val fix: String,
) : Serializable {
    val key: String get() = "$rule|$location|$message"
}

internal data class Snapshot(
    val modules: List<Module>,
    val nodes: List<Node>,
    val edges: List<Edge>,
    val sources: List<SourceRoot> = emptyList(),
    val problems: List<Finding> = emptyList(),
) : Serializable

internal fun Kind.isTest(): Boolean = this != Kind.MAIN

internal object BoundaryPolicy {
    fun violation(from: Module, to: Module, kind: Kind): String? {
        if (from.path == to.path) return null // Self dependencies are diagnosed by the cycle check.
        if (from.role == Role.UNKNOWN || to.role == Role.UNKNOWN) return "UNKNOWN_MODULE"
        if (to.path == ":core:testing" && !kind.isTest()) return "TESTING_IN_PRODUCTION"
        return when (from.role) {
            Role.CORE -> if (to.role != Role.CORE) "CORE_TO_FEATURE_OR_APP" else null
            Role.API -> when {
                to.role == Role.APP -> "FEATURE_TO_APP"
                to.role == Role.IMPL || to.path == ":core:data" -> "API_TO_IMPLEMENTATION"
                else -> null
            }
            Role.IMPL -> when {
                to.role == Role.APP -> "FEATURE_TO_APP"
                to.role == Role.IMPL -> "CROSS_FEATURE_IMPL"
                else -> null
            }
            Role.APP -> if (!kind.isTest() && to.role == Role.CORE && to.path !in setOf(
                    ":core:common", ":core:model", ":core:designsystem",
                )) "APP_COMPOSITION" else null
            Role.UNKNOWN -> "UNKNOWN_MODULE"
        }
    }

    fun fix(rule: String): String = when (rule) {
        "TESTING_IN_PRODUCTION" -> "Move core:testing to testImplementation/androidTestImplementation."
        "CORE_TO_FEATURE_OR_APP" -> "Move shared contracts into Core; Core must not depend on Feature or app."
        "FEATURE_TO_APP" -> "Pass navigation/actions through app callbacks or a Feature API."
        "CROSS_FEATURE_IMPL", "API_TO_IMPLEMENTATION", "API_TRANSITIVE_IMPLEMENTATION" ->
            "Depend on a Feature API/shared model and keep implementation/data access in the impl module."
        "APP_COMPOSITION" -> "Keep app production code as composition; put data/UI behavior in a Feature."
        else -> "Register the module/configuration/context explicitly in the architecture collector."
    }
}
