import java.io.File
import java.lang.IllegalArgumentException

fun main(args: Array<String>) {
    val initialState = readGolState("day24.txt")

    val gol = generateSequence(initialState) { it.run() }
        .scan(Pair(emptyList<GolState>(), false)) { acc, s ->
            Pair(acc.first + s, acc.first.contains(s))
        }
        .dropWhile { !it.second }

    print("Part 1: ")
    println(gol.first().first.last().biodiversity())

    print("Part 2: ")
    val part2 = generateSequence(mapOf(Pair(0, initialState.removeCenter()))) { states ->
        val newLow = states.keys.min()!! - 1
        val newHigh = states.keys.max()!! + 1

        val newStates = mapOf(Pair(newLow, GolState.empty)) + states + Pair(newHigh, GolState.empty)

        val updatedStates = newStates.mapValues {
            val (level, state) = it
            state.runRecursive(
                newStates.getOrDefault(level - 1, GolState.empty),
                newStates.getOrDefault(level + 1, GolState.empty)
            )
        }

        val lowestState = updatedStates[newLow]
        val highestState = updatedStates[newHigh]

        val withoutLowest = if (lowestState != null && lowestState.biodiversity() == 0L) {
            updatedStates - newLow
        } else {
            updatedStates
        }

        if (highestState != null && highestState.biodiversity() == 0L) {
            withoutLowest - newHigh
        } else {
            withoutLowest
        }
    }

    val result = part2.take(201).last()

    println(result.values.map { it.fields.count { it.value } }.sum())
}

data class GolPosition(val x: Int, val y: Int) {
    fun neighbours(): List<GolPosition> =
        listOf(
            copy(x = x + 1),
            copy(x = x - 1),
            copy(y = y + 1),
            copy(y = y - 1)
        )
}

data class GolState(val fields: Map<GolPosition, Boolean>) {
    private fun getValueAt(x: Int, y: Int): Boolean =
        fields.getValue(GolPosition(x, y))

    private fun getRow(y: Int) =
        fields
            .filter { it.key.y == y }
            .values

    private fun getColumn(x: Int) =
        fields
            .filter { it.key.x == x }
            .values

    fun run(): GolState {
        return GolState(fields.mapValues { field ->
            calculateNewState(
                field.key.neighbours().count { fields.getOrDefault(it, false) },
                field.value
            )
        })
    }

    fun runRecursive(lower: GolState, upper: GolState): GolState {
        return GolState(fields.mapValues { field ->
            val neighbours = field.key
                .neighbours()
                .flatMap { neighbour ->
                    when {
                        neighbour.x == -1 ->
                            listOf(upper.getValueAt(1, 2))
                        neighbour.x == 5 ->
                            listOf(upper.getValueAt(3, 2))
                        neighbour.y == -1 ->
                            listOf(upper.getValueAt(2, 1))
                        neighbour.y == 5 ->
                            listOf(upper.getValueAt(2, 3))
                        neighbour.x == 2 && neighbour.y == 2 ->
                            when {
                                field.key.x == 2 && field.key.y == 1 ->
                                    lower.getRow(0)
                                field.key.x == 2 && field.key.y == 3 ->
                                    lower.getRow(4)
                                field.key.x == 1 && field.key.y == 2 ->
                                    lower.getColumn(0)
                                field.key.x == 3 && field.key.y == 2 ->
                                    lower.getColumn(4)
                                else -> throw IllegalArgumentException("impossible")
                            }
                        else -> listOf(fields.getValue(neighbour))
                    }
                }

            calculateNewState(neighbours.count { it }, field.value)
        })
    }

    private fun calculateNewState(aliveCount: Int, currentState: Boolean): Boolean =
        when {
            aliveCount != 1 && currentState -> false
            (aliveCount == 1 || aliveCount == 2) && !currentState -> true
            else -> currentState
        }

    fun removeCenter(): GolState = GolState(fields.filterKeys { !(it.x == 2 && it.y == 2) })

    fun biodiversity(): Long =
        fields
            .filterValues { it }
            .keys
            .map {
                1L shl (it.x + it.y * 5)
            }
            .sum()

    companion object {
        val empty = GolState(IntRange(0, 4)
            .flatMap { y ->
                IntRange(0, 4)
                    .map { x -> Pair(GolPosition(x, y), false) }
            }
            .toMap())
            .removeCenter()
    }
}

fun readGolState(file: String): GolState {
    return GolState(File("inputs/$file")
        .readLines()
        .mapIndexed { y, line ->
            line.mapIndexed { x, c ->
                when (c) {
                    '#' -> Pair(GolPosition(x, y), true)
                    else -> Pair(GolPosition(x, y), false)
                }
            }
        }
        .flatten()
        .toMap()
    )
}