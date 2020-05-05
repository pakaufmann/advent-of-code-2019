fun main(args: Array<String>) {
    val program = readProgram("day19.txt")

    val beams = LongRange(0, 49).flatMap { y ->
        LongRange(0, 49).map { x ->
            runProgramIO(ProgramState(0, readProgram("day19.txt"), ListIO(listOf(x, y))))
                .last().io.writes.last()
        }
    }

    print("Part 1: ")
    println(beams.count { it == 1L })

    print("Part 2: ")
    val bottomLeft = beamTop(program)

    val topRightX = bottomLeft.first.first
    val topRightY = bottomLeft.first.second - 99

    println(topRightX * 10000 + topRightY)
}

private fun beamTop(program: Map<Long, Long>): Pair<Pair<Long, Long>, Long> {
    return generateSequence(100L) { it + 1 }.map { y ->
        val x = (y * 2)

        val bottomLeftX = x - 99
        val bottomLeftY = y + 99

        Pair(
            Pair(bottomLeftX, bottomLeftY),
            runProgramIO(ProgramState(0, program, ListIO(listOf(bottomLeftX, bottomLeftY)))).last().io.writes.last()
        )
    }.first { it.second == 1L }
}

data class ListIO(val reads: List<Long>, val writes: List<Long> = emptyList()) : ProgramIO<ListIO> {
    override fun write(out: Long): ListIO = copy(writes = writes + out)

    override fun read(): Pair<Long, ListIO> = Pair(reads.first(), copy(reads = reads.drop(1)))
}