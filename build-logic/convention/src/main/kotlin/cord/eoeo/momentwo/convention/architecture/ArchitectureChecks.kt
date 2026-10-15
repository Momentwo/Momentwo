package cord.eoeo.momentwo.convention.architecture

internal object ArchitectureChecks {
    fun boundaries(snapshot: Snapshot): List<Finding> {
        val modules = snapshot.modules.associateBy { it.path }
        val findings = snapshot.problems.toMutableList()
        snapshot.modules.filter { it.role == Role.UNKNOWN }.forEach {
            findings += Finding("UNKNOWN_MODULE", "Unclassified module ${it.path}", it.path, BoundaryPolicy.fix("UNKNOWN_MODULE"))
        }
        snapshot.edges.forEach { edge ->
            val rule = BoundaryPolicy.violation(modules.getValue(edge.from.project), modules.getValue(edge.to.project), edge.from.kind)
            if (rule != null) findings += Finding(rule, "${edge.from.key} --${edge.configuration}--> ${edge.to.key}",
                "${edge.from.project}:${edge.configuration}", BoundaryPolicy.fix(rule))
        }
        val adjacent = snapshot.edges.groupBy { it.from }
        snapshot.nodes.filter { it.kind == Kind.MAIN && modules.getValue(it.project).role == Role.API }.forEach { start ->
            val queue = java.util.ArrayDeque<Pair<Node, List<Edge>>>()
            val visited = mutableSetOf(start)
            queue.add(start to emptyList())
            while (queue.isNotEmpty()) {
                val (node, path) = queue.removeFirst()
                adjacent[node].orEmpty().sortedBy { it.to.key }.forEach { edge ->
                    if (visited.add(edge.to)) {
                        val next = path + edge
                        if (modules.getValue(edge.to.project).role == Role.IMPL || edge.to.project == ":core:data") {
                            findings += Finding("API_TRANSITIVE_IMPLEMENTATION", renderPath(next), start.key,
                                BoundaryPolicy.fix("API_TRANSITIVE_IMPLEMENTATION"))
                        }
                        queue.add(edge.to to next)
                    }
                }
            }
        }
        return findings.distinctBy { it.key }.sortedBy { it.key }
    }

    fun cycles(snapshot: Snapshot): List<Finding> {
        val adjacent = snapshot.edges.groupBy { it.from }.mapValues { (_, edges) ->
            edges.distinctBy { it.to }.sortedBy { it.to.key }
        }
        var index = 0
        val indices = mutableMapOf<Node, Int>()
        val low = mutableMapOf<Node, Int>()
        val stack = java.util.ArrayDeque<Node>()
        val onStack = mutableSetOf<Node>()
        val groups = mutableListOf<Set<Node>>()
        fun visit(node: Node) {
            indices[node] = index; low[node] = index; index++
            stack.push(node); onStack.add(node)
            adjacent[node].orEmpty().forEach { edge ->
                if (edge.to !in indices) { visit(edge.to); low[node] = minOf(low.getValue(node), low.getValue(edge.to)) }
                else if (edge.to in onStack) low[node] = minOf(low.getValue(node), indices.getValue(edge.to))
            }
            if (low[node] == indices[node]) {
                val group = mutableSetOf<Node>()
                do { val member = stack.pop(); onStack.remove(member); group.add(member) } while (member != node)
                if (group.size > 1 || adjacent[node].orEmpty().any { it.to == node }) groups += group
            }
        }
        snapshot.nodes.sortedBy { it.key }.forEach { if (it !in indices) visit(it) }
        return (snapshot.problems + groups.map { group ->
            val start = group.minBy { it.key }
            fun pathToStart(node: Node, seen: Set<Node>): List<Edge>? {
                for (edge in adjacent[node].orEmpty().filter { it.to in group }) {
                    if (edge.to == start) return listOf(edge)
                    if (edge.to !in seen) {
                        val rest = pathToStart(edge.to, seen + edge.to)
                        if (rest != null) return listOf(edge) + rest
                    }
                }
                return null
            }
            val path = requireNotNull(pathToStart(start, setOf(start)))
            Finding("DEPENDENCY_CYCLE", "Members: ${group.map { it.key }.sorted().joinToString()}\n${renderPath(path)}", start.key,
                "Break the cycle by extracting a shared contract/API or reversing a callback; keep build contexts separate.")
        }).distinctBy { it.key }.sortedBy { it.key }
    }

    private fun renderPath(edges: List<Edge>): String = edges.first().from.key + edges.joinToString("") {
        " --${it.configuration}--> ${it.to.key}"
    }
}
