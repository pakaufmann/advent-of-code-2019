import java.io.File
import java.lang.IllegalArgumentException
import kotlin.math.pow

fun main(args: Array<String>) {
    val initialState = ProgramState(0, readProgram("day2.txt"), PrintIO)
    val solution = runProgramIO(initialState.writeValue(1, 12).writeValue(2, 2))
        .last()

    print("Part 1: ")
    println(solution.program[0])

    val part2 = part2(initialState)
    print("Part 2: ")
    println(100 * part2!!.first + part2.second)
}

private fun part2(start: ProgramState<PrintIO>): Triple<Int, Int, ProgramState<PrintIO>?>? {
    return generateSequence(1) { it + 1 }
        .flatMap { noun ->
            IntRange(1, noun)
                .map { verb ->
                    Triple(
                        noun,
                        verb,
                        runProgramIO(start.writeValue(1, noun.toLong()).writeValue(2, verb.toLong())).last()
                    )
                }
                .asSequence()
        }
        .find { it.third.program[0] == 19690720L }
}

fun <T : ProgramIO<T>> runProgramIO(start: ProgramState<T>): Sequence<ProgramState<T>> =
    generateSequence(start) { it.runStep() }
        .takeWhileInclusive { !it.finished() }

interface ProgramIO<T> {
    fun write(out: Long): T

    fun read(): Pair<Long, T>
}

object PrintIO : ProgramIO<PrintIO> {
    override fun write(out: Long): PrintIO {
        println(out)
        return this
    }

    override fun read(): Pair<Long, PrintIO> = Pair(readLine()!!.toLong(), this)
}

data class ProgramState<T : ProgramIO<T>>(
    val ip: Long,
    val program: Map<Long, Long>,
    val io: T,
    val relativeBase: Long = 0
) {
    fun finished(): Boolean = program[ip] == 99L

    fun runStep(): ProgramState<T> {
        return when (currentInstruction().toInt() % 100) {
            1 ->
                writeParameter(3, readParameter(1) + readParameter(2))
                    .advance(4)
            2 ->
                writeParameter(3, readParameter(1) * readParameter(2))
                    .advance(4)
            3 -> {
                val (read, newIO) = io.read()
                writeParameter(1, read)
                    .advance(2, newIO = newIO)
            }
            4 -> {
                advance(2, newIO = io.write(readParameter(1)))
            }
            5 ->
                advanceAbsolute(if (readParameter(1) != 0L) readParameter(2) else ip + 3)
            6 ->
                advanceAbsolute(if (readParameter(1) == 0L) readParameter(2) else ip + 3)
            7 ->
                writeParameter(3, if (readParameter(1) < readParameter(2)) 1 else 0)
                    .advance(4)
            8 ->
                writeParameter(3, if (readParameter(1) == readParameter(2)) 1 else 0)
                    .advance(4)
            9 -> {
                advance(2, changeBaseBy = readParameter(1))
            }
            99 -> this
            else -> throw IllegalArgumentException("Invalid program state: '${program[ip]}'")
        }
    }

    private fun advanceAbsolute(to: Long): ProgramState<T> = copy(ip = to)

    private fun advance(by: Int, changeBaseBy: Long = 0, newIO: T = io): ProgramState<T> =
        copy(ip = ip + by, relativeBase = relativeBase + changeBaseBy, io = newIO)

    private fun mode(instruction: Long, position: Long): Long =
        (instruction % (10.0.pow(position + 2.0).toInt()) / (10.0.pow(position + 1.0).toInt()))

    private fun readParameter(parameterPosition: Long): Long =
        when (mode(currentInstruction(), parameterPosition)) {
            0L -> readAbsolute(readFromIp(parameterPosition))
            1L -> readFromIp(parameterPosition)
            2L -> readFromRelativeBase(readFromIp(parameterPosition))
            else -> throw IllegalArgumentException("Illegal mode")
        }

    private fun writeParameter(position: Long, newValue: Long): ProgramState<T> {
        val address = when (mode(currentInstruction(), position)) {
            0L -> readFromIp(position)
            2L -> relativeBase + readFromIp(position)
            else -> throw IllegalArgumentException("Illegal mode")
        }

        return this.copy(program = program.plus(Pair(address, newValue)))
    }

    private fun readAbsolute(position: Long) = program.getOrDefault(position, 0)

    private fun readFromIp(offset: Long) = program.getOrDefault(ip + offset, 0)

    private fun readFromRelativeBase(offset: Long) = program.getOrDefault(relativeBase + offset, 0)

    private fun currentInstruction() = program.getOrDefault(ip, 0)

    fun writeValue(index: Long, newValue: Long): ProgramState<T> {
        return this.copy(program = program.plus(Pair(index, newValue)))
    }

    fun <U : ProgramIO<U>> changeIO(newIO: U): ProgramState<U> =
        ProgramState(ip, program, newIO, relativeBase)
}

fun runProgram(start: State, inputList: Sequence<Long> = emptySequence()): Sequence<State> =
    generateSequence(Pair(start, inputList)) { it.first.runStep(it.second) }
        .map { it.first }
        .takeWhileInclusive { !it.finished() }

fun readProgram(file: String): Map<Long, Long> =
    File("inputs/$file")
        .readLines()
        .first()
        .split(",")
        .map { it.toLong() }
        .withIndex()
        .associate { Pair(it.index.toLong(), it.value) }

data class State(val ip: Long, val program: Map<Long, Long>, val output: Long? = null, val relativeBase: Long = 0) {
    fun finished(): Boolean = program[ip] == 99L

    fun runStep(readList: Sequence<Long>): Pair<State, Sequence<Long>> {
        return when (currentInstruction().toInt() % 100) {
            1 ->
                Pair(
                    writeParameter(3, readParameter(1) + readParameter(2))
                        .advance(4),
                    readList
                )
            2 ->
                Pair(
                    writeParameter(3, readParameter(1) * readParameter(2))
                        .advance(4),
                    readList
                )
            3 -> {
                Pair(
                    writeParameter(1, readList.first()).advance(2),
                    readList.drop(1)
                )
            }
            4 -> {
                Pair(advance(2, readParameter(1)), readList)
            }
            5 ->
                Pair(
                    advanceAbsolute(if (readParameter(1) != 0L) readParameter(2) else ip + 3),
                    readList
                )
            6 ->
                Pair(
                    advanceAbsolute(if (readParameter(1) == 0L) readParameter(2) else ip + 3),
                    readList
                )
            7 ->
                Pair(
                    writeParameter(3, if (readParameter(1) < readParameter(2)) 1 else 0)
                        .advance(4),
                    readList
                )
            8 ->
                Pair(
                    writeParameter(3, if (readParameter(1) == readParameter(2)) 1 else 0)
                        .advance(4),
                    readList
                )
            9 -> {
                Pair(
                    advance(2, changeBaseBy = readParameter(1)),
                    readList
                )
            }
            99 -> Pair(this, readList)
            else -> throw IllegalArgumentException("Invalid program state: '${program[ip]}'")
        }
    }

    private fun advanceAbsolute(to: Long): State = copy(ip = to, output = null)

    private fun advance(by: Int, output: Long? = null, changeBaseBy: Long = 0): State =
        copy(ip = ip + by, output = output, relativeBase = relativeBase + changeBaseBy)

    private fun mode(instruction: Long, position: Long): Long =
        (instruction % (10.0.pow(position + 2.0).toInt()) / (10.0.pow(position + 1.0).toInt()))

    private fun readParameter(parameterPosition: Long): Long =
        when (mode(currentInstruction(), parameterPosition)) {
            0L -> readAbsolute(readFromIp(parameterPosition))
            1L -> readFromIp(parameterPosition)
            2L -> readFromRelativeBase(readFromIp(parameterPosition))
            else -> throw IllegalArgumentException("Illegal mode")
        }

    private fun writeParameter(position: Long, newValue: Long): State {
        val address = when (mode(currentInstruction(), position)) {
            0L -> readFromIp(position)
            2L -> relativeBase + readFromIp(position)
            else -> throw IllegalArgumentException("Illegal mode")
        }

        return this.copy(program = program.plus(Pair(address, newValue)))
    }

    private fun readAbsolute(position: Long) = program.getOrDefault(position, 0)

    private fun readFromIp(offset: Long) = program.getOrDefault(ip + offset, 0)

    private fun readFromRelativeBase(offset: Long) = program.getOrDefault(relativeBase + offset, 0)

    private fun currentInstruction() = program.getOrDefault(ip, 0)

    fun writeValue(index: Long, newValue: Long): State {
        return this.copy(program = program.plus(Pair(index, newValue)))
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