fun main(args: Array<String>) {
    val program = readProgram("day17.txt")

    print("Part 1: ")
    val result = runProgramIO(ProgramState(0, program, MapIO())).last().io
    val scaffolds = result.scaffolds
    println(findIntersections(scaffolds).map { it.x * it.y }.sum())

    print("Part 2: ")
    val path = generatePath(scaffolds.toSet(), result.robotPosition!!, Direction.UP)
    val compacted = compactMoves(path)

    val (main, subroutines) = findMatchingSubroutines(compacted)!!
    println(
        runProgramIO(
            ProgramState(0, program, SubroutineIO.fromRoutines(main, subroutines))
                .writeValue(0, 2)
        ).dropWhile { !it.finished() }.first().io.output
    )
}

data class SubroutineIO(val writes: List<Long>, val output: Long?) : ProgramIO<SubroutineIO> {
    override fun write(out: Long): SubroutineIO {
        return copy(output = out)
    }

    override fun read(): Pair<Long, SubroutineIO> =
        Pair(writes.first(), copy(writes = writes.drop(1)))

    companion object {
        fun fromRoutines(main: List<String>, subroutines: List<Subroutine>): SubroutineIO {
            val mainWrites = main.joinToString(",")

            val subA = subroutines.first { it.name == "A" }.path.map { it.asInput() }.joinToString(",")
            val subB = subroutines.first { it.name == "B" }.path.map { it.asInput() }.joinToString(",")
            val subC = subroutines.first { it.name == "C" }.path.map { it.asInput() }.joinToString(",")

            val all = mainWrites.map { it.toLong() } + 10L +
                    subA.map { it.toLong() } + 10L +
                    subB.map { it.toLong() } + 10L +
                    subC.map { it.toLong() } + 10L +
                    'n'.toLong() + 10L

            return SubroutineIO(all, null)
        }
    }
}

private fun findMatchingSubroutines(compacted: List<Compacted>): Pair<List<String>, List<Subroutine>>? =
    generateSubroutine("A", compacted) { subA ->
        generateSubroutine("B", dropFromStart(compacted, listOf(subA))) { subB ->
            generateSubroutine("C", dropFromStart(compacted, listOf(subA, subB))) { subC ->
                val main = buildMainRoutine(compacted, listOf(subA, subB, subC))
                if (main != null) {
                    Pair(main, listOf(subA, subB, subC))
                } else {
                    null
                }
            }
        }
    }

private fun dropFromStart(compact: List<Compacted>, subroutines: List<Subroutine>): List<Compacted> {
    var rest = compact
    while (true) {
        val found = subroutines.find { rest.take(it.path.size) == it.path }
        if (found != null) {
            rest = rest.drop(found.path.size)
        } else {
            return rest
        }
    }
}

private fun generateSubroutine(
    subName: String,
    compacted: List<Compacted>,
    next: (Subroutine) -> Pair<List<String>, List<Subroutine>>?
): Pair<List<String>, List<Subroutine>>? =
    IntRange(1, compacted.size / 2).map { length ->
        val duplicate = findDuplicates(compacted, length)
        if (duplicate != null) {
            next(Subroutine(subName, duplicate))
        } else {
            null
        }
    }.find { it != null }

fun buildMainRoutine(path: List<Compacted>, subroutines: List<Subroutine>): List<String>? {
    if (path.isEmpty()) {
        return emptyList()
    }

    val sub = subroutines.find { path.take(it.path.size) == it.path } ?: return null
    val rest = buildMainRoutine(path.drop(sub.path.size), subroutines) ?: return null
    return listOf(sub.name) + rest
}

data class Subroutine(val name: String, val path: List<Compacted>)

fun findDuplicates(
    path: List<Compacted>,
    length: Int,
    existing: Set<List<Compacted>> = emptySet()
): List<Compacted>? {
    val toFind = path.take(length)
    if (path.drop(length).windowed(length).any { it == toFind && !existing.contains(it) }) {
        return toFind
    }
    return null
}

interface Compacted {
    fun asInput(): String
}

object Right : Compacted {
    override fun asInput(): String = "R"
}

object Left : Compacted {
    override fun asInput(): String = "L"
}

data class Move(val steps: Int) : Compacted {
    fun increase(): Move = copy(steps = steps + 1)

    override fun asInput(): String = steps.toString()
}

fun compactMoves(movements: List<Movement>): List<Compacted> {
    return movements.fold(emptyList<Compacted>()) { acc, next ->
        when (next) {
            Movement.RIGHT -> acc + Right
            Movement.LEFT -> acc + Left
            Movement.MOVE -> {
                if (acc.lastOrNull() is Move) {
                    val last = acc.last() as Move
                    acc.dropLast(1) + last.increase()
                } else {
                    acc + Move(1)
                }
            }
        }
    }
}

enum class Movement {
    RIGHT, LEFT, MOVE;
}

fun generatePath(path: Set<Pos>, initial: Pos, initialDir: Direction): List<Movement> {
    tailrec fun pathRecursive(
        unvisited: Set<Pos>,
        robotPosition: Pos,
        direction: Direction,
        movements: List<Movement>
    ): List<Movement> =
        when {
            unvisited.isEmpty() -> movements.dropLast(1)
            path.contains(robotPosition.nextInDirection(direction)) -> {
                pathRecursive(
                    unvisited - robotPosition,
                    robotPosition.nextInDirection(direction),
                    direction,
                    movements + Movement.MOVE
                )
            }
            path.contains(robotPosition.leftNeighbour(direction)) ->
                pathRecursive(
                    unvisited - robotPosition,
                    robotPosition,
                    direction.turnLeft(),
                    movements + Movement.LEFT
                )
            else ->
                pathRecursive(
                    unvisited - robotPosition,
                    robotPosition,
                    direction.turnRight(),
                    movements + Movement.RIGHT
                )
        }

    return pathRecursive(path, initial, initialDir, emptyList())
}

fun findIntersections(scaffolds: List<Pos>): List<Pos> {
    return scaffolds.filter { scaffold ->
        scaffolds.containsAll(scaffold.neighbours())
    }
}

data class Pos(val x: Int, val y: Int) {
    fun neighbours(): List<Pos> = listOf(
        copy(x = x - 1),
        copy(x = x + 1),
        copy(y = y - 1),
        copy(y = y + 1)
    )

    fun nextInDirection(direction: Direction) = when (direction) {
        Direction.UP -> copy(y = y - 1)
        Direction.LEFT -> copy(x = x - 1)
        Direction.DOWN -> copy(y = y + 1)
        Direction.RIGHT -> copy(x = x + 1)
    }

    fun right(): Pos = copy(x = x + 1)

    fun newLine(): Pos = copy(x = 0, y = y + 1)

    fun leftNeighbour(direction: Direction): Pos = when (direction) {
        Direction.UP -> copy(x = x - 1)
        Direction.LEFT -> copy(y = y + 1)
        Direction.DOWN -> copy(x = x + 1)
        Direction.RIGHT -> copy(y = y - 1)
    }
}

data class MapIO(
    val scaffolds: List<Pos> = emptyList(),
    val robotPosition: Pos? = null,
    val currentPosition: Pos = Pos(0, 0)
) : ProgramIO<MapIO> {
    override fun write(out: Long): MapIO =
        when (out) {
            35L -> copy(scaffolds = scaffolds + currentPosition, currentPosition = currentPosition.right())
            94L -> copy(
                scaffolds = scaffolds + currentPosition,
                currentPosition = currentPosition.right(),
                robotPosition = currentPosition
            )
            46L -> copy(currentPosition = currentPosition.right())
            10L -> copy(currentPosition = currentPosition.newLine())
            else -> throw IllegalArgumentException("Illegal input: $out")
        }

    override fun read(): Pair<Long, MapIO> = Pair(0L, this)
}