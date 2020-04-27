import java.io.File
import java.io.InputStream
import java.io.PrintStream
import java.lang.IllegalArgumentException
import java.nio.charset.Charset
import kotlin.math.pow

fun main(args: Array<String>) {
    val initialState = State(0, readProgram("day2.txt"))
    val solution = runProgram(initialState.writeValue(1, 12).writeValue(2, 2))

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
                        runProgram(start.writeValue(1, noun).writeValue(2, verb))
                    )
                }
                .asSequence()
        }
        .find { it.third!!.program[0] == 19690720 }
}

fun runProgram(start: State, printStream: PrintStream = System.out, readStream: InputStream = System.`in`) =
    generateSequence(start) { it.runStep(printStream, readStream) }
        .find { it.finished() }

fun readProgram(file: String) =
    File("inputs/$file")
        .readLines()
        .first()
        .split(",")
        .map { it.toInt() }

data class State(val ip: Int, val program: List<Int>) {
    fun finished(): Boolean = program[ip] == 99

    fun runStep(printStream: PrintStream, readStream: InputStream): State {
        return when (program[ip] % 10) {
            1 -> {
                writeValue(
                    program[ip + 3],
                    readValue(1) + readValue(2)
                )
                    .copy(ip = ip + 4)
            }
            2 ->
                writeValue(
                    program[ip + 3],
                    readValue(1) * readValue(2)
                )
                    .copy(ip = ip + 4)
            3 ->
                writeValue(program[ip + 1], readStream.bufferedReader(Charset.defaultCharset()).readLine().toInt())
                    .copy(ip = ip + 2)
            4 -> {
                printStream.println(readValue(1))
                copy(ip = ip + 2)
            }
            5 ->
                copy(
                    ip = if (readValue(1) != 0) {
                        readValue(2)
                    } else {
                        ip + 3
                    }
                )
            6 ->
                copy(
                    ip = if (readValue(1) == 0) {
                        readValue(2)
                    } else {
                        ip + 3
                    }
                )
            7 ->
                writeValue(program[ip + 3], if (readValue(1) < readValue(2)) 1 else 0)
                    .copy(ip = ip + 4)
            8 ->
                writeValue(program[ip + 3], if (readValue(1) == readValue(2)) 1 else 0)
                    .copy(ip = ip + 4)
            99 -> this
            else -> throw IllegalArgumentException("Invalid program state: ${program[ip]}")
        }
    }

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