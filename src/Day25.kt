fun main(args: Array<String>) {
    val program = readProgram("day25.txt")

    val inputs = """
        east
        take jam
        east
        take fuel cell
        west
        south
        take shell
        north
        west
        south
        west
        take easter egg
        north
        east
        take space heater
        west
        south
        west
        west
        south
        west
        north
        take coin
        south
        east
        north
        take monolith
        west
        take mug
        north
        drop coin
        drop easter egg
        drop monolith
        drop jam
        drop mug
        drop space heater
        drop fuel cell
        drop shell
    """.trimIndent()

    val allItems = setOf(
        "take coin",
        "take easter egg",
        "take monolith",
        "take jam",
        "take mug",
        "take space heater",
        "take fuel cell",
        "take shell"
    )

    //change to ASCIIInteractiveIO to get a prompt
    val atFrontDoor = runProgramIO(ProgramState(0, program, ListIO(inputs.map { it.toLong() } + 10L)))
        .dropWhile { it.io.reads.isNotEmpty() }
        .first()

    val heavier = "heavier".map { it.toLong() }
    val lighter = "lighter".map { it.toLong() }
    val airlock = "airlock".map { it.toLong() }

    val result = generateSequence(4) { it + 1 }
        .flatMap { size ->
            allItems
                .combinations(size)
                .asSequence()
                .map { combination ->
                    val input = combination.joinToString("\n").map { it.toLong() } + 10L +
                            "north".map { it.toLong() } + 10L

                    runProgramIO(atFrontDoor.changeIO(ListIO(input)))
                        .dropWhile {
                            val last = it.io.writes.takeLast(7)
                            last != heavier && last != lighter && last != airlock
                        }
                        .first()
                        .io
                        .writes
                        .map { it.toChar() }
                        .joinToString("")
                }
        }
        .dropWhile { !it.contains("airlock") }
        .first()

    print("Part 1: ")
    println(result)

    print("Part 2: ")
}

// taken from https://github.com/MarcinMoskala/KotlinDiscreteMathToolkit/
fun <T> Set<T>.combinations(combinationSize: Int): Set<Set<T>> = when {
    combinationSize < 0 -> throw Error("combinationSize cannot be smaller then 0. It is equal to $combinationSize")
    combinationSize == 0 -> setOf(setOf())
    combinationSize >= size -> setOf(toSet())
    else -> powerset()
        .filter { it.size == combinationSize }
        .toSet()
}

fun <T> Collection<T>.powerset(): Set<Set<T>> = powerset(this, setOf(setOf()))

private tailrec fun <T> powerset(left: Collection<T>, acc: Set<Set<T>>): Set<Set<T>> = when {
    left.isEmpty() -> acc
    else -> powerset(left.drop(1), acc + acc.map { it + left.first() })
}

data class ASCIIInteractiveIO(val buffer: List<Long> = emptyList()) : ProgramIO<ASCIIInteractiveIO> {
    override fun write(out: Long): ASCIIInteractiveIO {
        print(out.toChar())
        return this
    }

    override fun read(): Pair<Long, ASCIIInteractiveIO> {
        val newBuffer = if (buffer.isEmpty()) {
            readLine()!!.toList().map { it.toLong() } + 10L
        } else {
            buffer
        }
        print(newBuffer.first().toChar())

        return Pair(newBuffer.first(), copy(buffer = newBuffer.drop(1)))
    }
}