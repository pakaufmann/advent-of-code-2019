import java.io.File

fun main(args: Array<String>) {
    val inputFile = "day20.txt"

    val maze = readMaze(inputFile)
    val (start, end, portals) = readPortals(inputFile, maze)

    val part1 = searchMaze(maze, portals, start)
        .map { it.toSearch.first() }
        .dropWhile { it.position != end }
        .first()
        .steps

    print("Part 1: ")
    println(part1)

    print("Part 2: ")

    val part2 = searchMazeRecursive(maze, portals, start)
        .map { it.toSearch.first() }
        .dropWhile { !(it.position == end && it.level == 0) }
        .first()
        .steps

    println(part2)
}

fun searchMazeRecursive(
    validPositions: Set<MazePosition>,
    portals: List<Portal>,
    startPosition: MazePosition
): Sequence<MazeState> {
    return generateSequence(MazeState(mutableListOf(MazeSearch(startPosition)))) { state ->
        val next = state.next()
        val neighbours = next.position.neighbours()

        val visitedPair = Pair(next.position, next.level)
        if (state.visited.contains(visitedPair)) {
            state.update(emptyList(), visitedPair)
        } else {
            val possibleNeighbours = neighbours.filter { validPositions.contains(it) }
            val possibleDescendingPortals = portals.filter { it.inner == next.position }.map { it.outer }
            val possibleAscendingPortals = portals
                .filter { it.outer == next.position && next.level != 0 }
                .map { it.inner }

            val toVisit = possibleNeighbours
                .filter { !state.visited.contains(Pair(it, next.level)) && validPositions.contains(it) }
                .map { MazeSearch(it, next.steps + 1, next.level) } +
                    possibleDescendingPortals
                        .filter { !state.visited.contains(Pair(it, next.level - 1)) }
                        .map { MazeSearch(it, next.steps + 1, next.level - 1) } +
                    possibleAscendingPortals
                        .filter { !state.visited.contains(Pair(it, next.level + 1)) }
                        .map { MazeSearch(it, next.steps + 1, next.level + 1) }

            state.update(toVisit, visitedPair)
        }
    }
}

fun searchMaze(
    validPositions: Set<MazePosition>,
    portals: List<Portal>,
    startPosition: MazePosition
): Sequence<MazeState> {
    return generateSequence(MazeState(mutableListOf(MazeSearch(startPosition)))) { state ->
        val next = state.next()

        val neighbours = next.position.neighbours()

        val possibleNeighbours = neighbours.filter { validPositions.contains(it) }
        val possiblePortals = portals.filter { it.outer == next.position }.map { it.inner } +
                portals.filter { it.inner == next.position }.map { it.outer }

        val toVisit = (possibleNeighbours + possiblePortals)
            .filter { !state.visited.contains(Pair(it, 0)) && validPositions.contains(it) }
            .map { MazeSearch(it, next.steps + 1) }

        state.update(toVisit, Pair(next.position, 0))
    }
}

data class MazeState(
    val toSearch: MutableList<MazeSearch>,
    val visited: MutableSet<Pair<MazePosition, Int>> = hashSetOf()
) {
    fun next(): MazeSearch = toSearch.first()

    fun update(toVisit: List<MazeSearch>, visited: Pair<MazePosition, Int>): MazeState {
        toSearch.removeAt(0)
        toSearch.addAll(toVisit)
        this.visited.add(visited)
        return this
    }
}

data class MazeSearch(val position: MazePosition, val steps: Int = 0, val level: Int = 0)

data class MazePosition(val x: Int, val y: Int) {
    fun neighbours(): List<MazePosition> =
        listOf(
            copy(x = x + 1),
            copy(x = x - 1),
            copy(y = y + 1),
            copy(y = y - 1)
        )
}

data class Portal(val outer: MazePosition, val inner: MazePosition)

fun readPortals(file: String, maze: Set<MazePosition>): Triple<MazePosition, MazePosition, List<Portal>> {
    val possiblePositions = File("inputs/$file")
        .readLines()
        .mapIndexed { y, line ->
            line.mapIndexed { x, char ->
                if (char.isLetter()) {
                    Pair(MazePosition(x, y), char)
                } else {
                    null
                }
            }
                .filterNotNull()
        }
        .flatten()
        .toSet()

    val portalPoints = possiblePositions
        .mapNotNull { portal ->
            val neighbours = portal.first.neighbours()
            val from = neighbours.find { maze.contains(it) }
            if (from != null) {
                val other = possiblePositions.first { neighbours.contains(it.first) }

                val name = listOf(portal, other).sortedBy { it.first.y + it.first.x }
                    .fold("") { acc, p -> acc + p.second }
                Pair(name, from)
            } else {
                null
            }
        }

    val portalGroups = portalPoints.groupBy { it.first }
    val outerY = maze.maxBy { it.y }!!.y
    val outerX = maze.maxBy { it.x }!!.x

    return Triple(
        portalPoints.first { it.first == "AA" }.second,
        portalPoints.first { it.first == "ZZ" }.second,
        portalGroups.filter { it.value.size == 2 }.map {
            val outer =
                it.value.first { it.second.y == 2 || it.second.y == outerY || it.second.x == 2 || it.second.x == outerX }
            val inner = it.value.first { it != outer }

            Portal(outer.second, inner.second)
        }
    )
}

fun readMaze(file: String): Set<MazePosition> =
    File("inputs/$file")
        .readLines()
        .mapIndexed { y, line ->
            line.mapIndexed { x, char ->
                when (char) {
                    '.' -> MazePosition(x, y)
                    else -> null
                }
            }.filterNotNull()
        }
        .flatten()
        .toSet()
