fun main(args: Array<String>) {
    val program = ProgramState(0, readProgram("day5.txt"), StaticIO(1))

    print("Part 1: ")
    println(runProgramWithState(program).last().io.write.last())

    print("Part 2: ")
    println(runProgramWithState(program.changeIO(StaticIO(5))).last().io.write.last())
}

data class StaticIO(val read: Long, val write: List<Long> = emptyList()) : ProgramIO<StaticIO> {
    override fun write(out: Long): StaticIO {
        return copy(write = write + out)
    }

    override fun read(): Pair<Long, StaticIO> = Pair(read, this)
}