fun main(args: Array<String>) {
    val program = State(0, readProgram("day5.txt"))

    print("Part 1: ")
    println(runProgram(program, listOf(1)).flatMap { toEmpty(it.output) }.toList())

    print("Part 2: ")
    println(runProgram(program, listOf(5)).flatMap { toEmpty(it.output) }.toList())
}

fun toEmpty(s: Int?): Sequence<Int> = if (s == null) emptySequence() else sequenceOf(s)