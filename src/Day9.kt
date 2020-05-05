fun main(args: Array<String>) {
    val program = ProgramState(0, readProgram("day9.txt"), StaticIO(1))
    print("Part 1: ")
    println(runProgramWithState(program).last().io.write.joinToString())

    println("Part 2: ")
    println(runProgramWithState(program.changeIO(StaticIO(2))).last().io.write.joinToString())
}