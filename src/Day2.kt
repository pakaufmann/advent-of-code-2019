import java.io.File
import java.lang.IllegalArgumentException
import java.nio.charset.Charset
import kotlin.math.pow

fun main(args: Array<String>) {
    val initialState = State(0, readProgram("day2.txt"))
    val solution = runProgram(initialState.writeValue(1, 12).writeValue(2, 2))
        .last()

    print("Part 1: ")
    println(solution.program[0])

    val part2 = part2(initialState)
    print("Part 2: ")
    println(100 * part2!!.first + part2.second)
}

private fun part2(start: State): Triple<Int, Int, State?>? {
    return generateSequence(1) { it + 1 }
        .flatMap { noun ->
            IntRange(1, noun)
                .map { verb ->
                    Triple(
                        noun,
                        verb,
                        runProgram(start.writeValue(1, noun).writeValue(2, verb)).last()
                    )
                }
                .asSequence()
        }
        .find { it.third.program[0] == 19690720 }
}

fun runProgram(start: State, inputList: List<Int> = emptyList()): Sequence<State> =
    generateSequence(Pair(start, inputList)) { it.first.runStep(it.second) }
        .map { it.first }
        .takeWhileInclusive { !it.finished() }

fun readProgram(file: String) =
    File("inputs/$file")
        .readLines()
        .first()
        .split(",")
        .map { it.toInt() }

data class State(val ip: Int, val program: List<Int>, val output: Int? = null) {
    fun finished(): Boolean = program[ip] == 99

    fun runStep(readList: List<Int>): Pair<State, List<Int>> {
        return when (program[ip] % 100) {
            1 ->
                Pair(
                    writeValue(
                        program[ip + 3],
                        readValue(1) + readValue(2)
                    ).advance(4),
                    readList
                )
            2 ->
                Pair(
                    writeValue(
                        program[ip + 3],
                        readValue(1) * readValue(2)
                    ).advance(4),
                    readList
                )
            3 ->
                Pair(
                    writeValue(program[ip + 1], readList.first()).advance(2),
                    readList.drop(1)
                )
            4 -> {
                Pair(advance(2, readValue(1)), readList)
            }
            5 ->
                Pair(
                    State(if (readValue(1) != 0) readValue(2) else ip + 3, program),
                    readList
                )
            6 ->
                Pair(
                    State(if (readValue(1) == 0) readValue(2) else ip + 3, program),
                    readList
                )
            7 ->
                Pair(
                    writeValue(program[ip + 3], if (readValue(1) < readValue(2)) 1 else 0)
                        .advance(4),
                    readList
                )
            8 ->
                Pair(
                    writeValue(program[ip + 3], if (readValue(1) == readValue(2)) 1 else 0)
                        .advance(4),
                    readList
                )
            99 -> Pair(this, readList)
            else -> throw IllegalArgumentException("Invalid program state: '${program[ip]}'")
        }
    }

    private fun advance(by: Int, output: Int? = null): State = copy(ip = ip + by, output = output)

    private fun readMode(instruction: Int, position: Int): Int =
        (instruction % (10.0.pow(position + 2).toInt()) / (10.0.pow(position + 1).toInt()))

    private fun readValue(position: Int): Int {
        return when (readMode(program[ip], position)) {
            0 -> program[program[ip + position]]
            1 -> program[ip + position]
            else -> throw IllegalArgumentException("Illegal mode")
        }
    }

    fun writeValue(index: Int, newValue: Int): State {
        return this.copy(program = program.mapIndexed { i, value ->
            if (i == index) {
                newValue
            } else {
                value
            }
        })
    }
}

fun <T> Sequence<T>.takeWhileInclusive(
    predicate: (T) -> Boolean
): Sequence<T> {
    var shouldContinue = true
    return takeWhile {
        val result = shouldContinue
        shouldContinue = predicate(it)
        result
    }
}