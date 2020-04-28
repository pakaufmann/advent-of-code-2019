fun main(args: Array<String>) {
    val program = State(0, readProgram("day7.txt"))

    val max = setOf(0L, 1, 2, 3, 4).permutations()
        .map { runWithSettings(program, it).second }
        .max()

    print("Part 1: ")
    println(max)

    print("Part 2: ")

    val maxComplete = setOf(5L, 6, 7, 8, 9).permutations()
        .map { runToEnd(program, it).find { it.first.last().finished() }!!.second }

    println(maxComplete.max())
}

private fun runToEnd(start: State, phaseSequence: List<Long>): Sequence<Pair<List<State>, Long>> =
    generateSequence(runWithSettings(start, phaseSequence)) { run ->
        run.first.fold(Pair(emptyList(), run.second)) { acc, program ->
            val result = runProgram(program.copy(output = null), listOf(acc!!.second))
                .find { it.output != null || it.finished() }!!

            Pair(acc.first + result, result.output ?: acc.second)
        }
    }

private fun runWithSettings(program: State, phaseSequence: List<Long>): Pair<List<State>, Long> {
    return phaseSequence.fold(Pair(emptyList(), 0L)) { acc, setting ->
        val result = runProgram(program, listOf(setting, acc.second)).find { it.output != null }!!
        Pair(acc.first + result, result.output!!)
    }
}

// taken from https://github.com/MarcinMoskala/KotlinDiscreteMathToolkit
fun <T> Set<T>.permutations(): Set<List<T>> = toList().permutations()

fun <T> List<T>.permutations(): Set<List<T>> = when {
    isEmpty() -> setOf()
    size == 1 -> setOf(listOf(get(0)))
    else -> {
        val element = get(0)
        drop(1).permutations()
            .flatMap { sublist -> (0..sublist.size).map { i -> sublist.plusAt(i, element) } }
            .toSet()
    }
}

internal fun <T> List<T>.plusAt(index: Int, element: T): List<T> = when {
    index !in 0..size -> throw Error("Cannot put at index $index because size is $size")
    index == 0 -> listOf(element) + this
    index == size -> this + element
    else -> dropLast(size - index) + element + drop(index)
}