fun main(args: Array<String>) {
    val program = State(0, readProgram("day9.txt"))
    print("Part 1: ")
    println(runProgram(program, listOf(1)).filter { it.output != null }.map { it.output }.joinToString())

    println("Part 2: ")
    println(runProgram(program, listOf(2)).filter { it.output != null }.map { it.output }.joinToString())
}