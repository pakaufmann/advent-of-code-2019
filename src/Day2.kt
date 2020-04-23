import java.io.File
import java.lang.IllegalArgumentException

fun main(args: Array<String>) {
    val initialState = State(0, readStart("day2.txt"))
    val solution = runProgram(initialState.replace(1, 12).replace(2, 2))

    print("Part 1: ")
    println(solution!!.program[0])

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
                        runProgram(start.replace(1, noun).replace(2, verb))
                    )
                }
                .asSequence()
        }
        .find { it.third!!.program[0] == 19690720 }
}

private fun runProgram(start: State) =
    generateSequence(start) { it.runStep() }
        .find { it.finished() }

fun readStart(file: String) =
    File("inputs/$file")
        .readLines()
        .first()
        .split(",")
        .map { it.toInt() }

data class State(val ip: Int, val program: List<Int>) {
    fun finished(): Boolean = program[ip] == 99

    fun runStep(): State {
        val newProgram = when (program[ip]) {
            1 ->
                replace(program[ip + 3], program[program[ip + 1]] + program[program[ip + 2]])
            2 ->
                replace(program[ip + 3], program[program[ip + 1]] * program[program[ip + 2]])
            99 -> this
            else -> throw IllegalArgumentException("Invalid program state!")
        }
        return newProgram.copy(ip = ip + 4)
    }

    fun replace(index: Int, newValue: Int): State {
        return this.copy(program = program.mapIndexed { i, value ->
            if (i == index) {
                newValue
            } else {
                value
            }
        })
    }
}