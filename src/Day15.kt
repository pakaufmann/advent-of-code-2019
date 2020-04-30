fun main(args: Array<String>) {
    val start = State(0, readProgram("day15.txt"))

    val runRobot = generateSequence(MovementState(start)) { state ->
        val direction = state.nextMove()

        val program = runProgram(state.program.copy(output = null), sequenceOf(direction.number))
            .dropWhile { it.output == null }
            .first()

        val result = when (program.output!!) {
            0L -> state.addWall(direction)
            1L -> state.moveRobotTo(direction, MapType.FLOOR)
            2L -> state.moveRobotTo(direction, MapType.OXYGEN_SYSTEM)
            else -> throw IllegalArgumentException("not possible")
        }

        result.copy(program = program)
    }

    val map = runRobot.dropWhile { it.unknownReachables.isNotEmpty() }.first().map
    val oxygenPosition = map.filter { it.value == MapType.OXYGEN_SYSTEM }.keys.first()

    val validPositions = map
        .filter { it.value == MapType.FLOOR || it.value == MapType.OXYGEN_SYSTEM }
        .map { it.key }
        .toSet()

    print("Part 1: ")
    println(
        bfs(validPositions, MapPosition(0, 0))
            .map { it.toSearch.first() }
            .dropWhile { it.position != oxygenPosition }
            .first()
            .depth
    )

    print("Part 2: ")
    println(
        bfs(validPositions, oxygenPosition)
            .dropWhile { it.visited.size < validPositions.size - 1 }
            .first()
            .toSearch
            .first()
            .depth
    )
}

private fun bfs(validPositions: Set<MapPosition>, startPosition: MapPosition): Sequence<SearchState> {
    return generateSequence(SearchState(listOf(SearchPosition(startPosition)))) { state ->
        val next = state.next()
        val possibleNeighbours = MovementDirection.values().map { next.position.move(it) }

        val toVisit = possibleNeighbours
            .filter { !state.visited.contains(it) && validPositions.contains(it) }
            .map { SearchPosition(it, next.depth + 1) }

        state.update(toVisit, next.position)
    }
}

data class SearchPosition(val position: MapPosition, val depth: Int = 0)

data class SearchState(val toSearch: List<SearchPosition>, val visited: Set<MapPosition> = emptySet()) {
    fun next(): SearchPosition = toSearch.first()

    fun update(toVisit: List<SearchPosition>, visited: MapPosition): SearchState =
        SearchState(toSearch.drop(1) + toVisit, this.visited + visited)
}

enum class MapType {
    UNKNOWN, FLOOR, WALL, OXYGEN_SYSTEM;
}

data class MapPosition(val x: Int, val y: Int) {
    fun move(direction: MovementDirection): MapPosition =
        when (direction) {
            MovementDirection.NORTH -> copy(y = y - 1)
            MovementDirection.EAST -> copy(x = x + 1)
            MovementDirection.SOUTH -> copy(y = y + 1)
            MovementDirection.WEST -> copy(x = x - 1)
        }
}

enum class MovementDirection(val number: Long) {
    NORTH(1), EAST(4), SOUTH(2), WEST(3);
}

data class MovementState(
    val program: State,
    val robotPosition: MapPosition = MapPosition(0, 0),
    val map: Map<MapPosition, MapType> = mapOf(Pair(MapPosition(0, 0), MapType.FLOOR)),
    val path: List<MapPosition> = emptyList(),
    val unknownReachables: Set<MapPosition> = setOf(
        MapPosition(1, 0),
        MapPosition(0, 1),
        MapPosition(-1, 0),
        MapPosition(0, -1)
    )
) {
    fun nextMove(): MovementDirection {
        val surroundings = surroundings(robotPosition)
        val unknown = surroundings.find { it.second == MapType.UNKNOWN }

        return if (unknown == null) {
            val visited = surroundings.map { Pair(it.first, path.indexOf(robotPosition.move(it.first))) }
            visited.minBy { it.second }!!.first
        } else {
            unknown.first
        }
    }

    private fun surroundings(position: MapPosition): List<Pair<MovementDirection, MapType>> {
        return MovementDirection.values()
            .map { Pair(it, map.getOrDefault(position.move(it), MapType.UNKNOWN)) }
            .filter { it.second != MapType.WALL }
    }

    fun addWall(direction: MovementDirection): MovementState {
        val wallPosition = robotPosition.move(direction)
        return this.copy(
            map = map + Pair(wallPosition, MapType.WALL),
            unknownReachables = unknownReachables - wallPosition
        )
    }

    fun moveRobotTo(direction: MovementDirection, mapType: MapType): MovementState {
        val position = robotPosition.move(direction)

        return this.copy(
            robotPosition = position,
            map = map + Pair(position, mapType),
            unknownReachables = unknownReachables - position + unknownReachables(position),
            path = path + robotPosition
        )
    }

    private fun unknownReachables(floorPosition: MapPosition): Set<MapPosition> {
        return surroundings(floorPosition).filter { it.second == MapType.UNKNOWN }
            .map { floorPosition.move(it.first) }
            .toSet() - floorPosition
    }
}