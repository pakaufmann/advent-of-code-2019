fun main(args: Array<String>) {
    val walkProgram = """NOT C J
        |AND D J
        |NOT A T
        |OR T J
        |WALK
        |"""".trimMargin()

    val walk = ProgramState(0, readProgram("day21.txt"), ListIO(walkProgram.map { it.toLong() }))

    print("Part 1: ")
    println(runProgramIO(walk).last().io.writes.last())

    print("Part 2: ")

    val runProgram = """NOT C J
        |AND D J
        |AND H J
        |NOT B T
        |AND D T
        |OR T J
        |NOT A T
        |OR T J
        |RUN
        |"""".trimMargin()

    val run = ProgramState(0, readProgram("day21.txt"), ListIO(runProgram.map { it.toLong() }))

    println(runProgramIO(run).last().io.writes.last())
}