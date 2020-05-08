import java.lang.IllegalArgumentException

fun main(args: Array<String>) {
    val robot = State(0, readProgram("day11.txt"))

    val registrationId = runRobot(robot, mapOf(Pair(Position(0, 0), Color.WHITE))).last().panels

    val maxX = registrationId.map { it.key.x }.max()!!
    val maxY = registrationId.map { it.key.y }.max()!!

    print("Part 1: ")
    println(runRobot(robot).last().panels.size)

    println("Part 2: ")
    IntRange(0, maxY)
        .map { y ->
            IntRange(0, maxX)
                .map { x ->
                    if (registrationId.get(Position(x, y)) == Color.WHITE) {
                        '#'
                    } else {
                        ' '
                    }
                }
                .joinToString("")
        }
        .forEach { println(it) }
}

private fun runRobot(robot: State, initialMap: Map<Position, Color> = emptyMap()): Sequence<RobotState> {
    return generateSequence(RobotState(robot, Position(0, 0), Direction.UP, initialMap)) { robotState ->
        val currentColor = robotState.getCurrentColor().number.toLong()
        val outputs = runProgram(robotState.robot, sequenceOf(currentColor))
            .filter { it.output != null || it.finished() }
            .take(2)
            .toList()

        if (outputs[0].finished()) {
            robotState.copy(robot = outputs[0])
        } else {
            val newDirection = when (outputs[1].output!!) {
                0L -> robotState.direction.turnLeft()
                1L -> robotState.direction.turnRight()
                else -> throw IllegalArgumentException("Invalid turn value")
            }

            val newPosition = robotState.position.move(newDirection)
            val panels = robotState.panels + Pair(robotState.position, Color.fromNumber(outputs[0].output!!))

            RobotState(outputs[1].copy(output = null), newPosition, newDirection, panels)
        }
    }.takeWhileInclusive { !it.robot.finished() }
}

enum class Direction {
    UP, LEFT, DOWN, RIGHT;

    fun turnLeft(): Direction =
        when (this) {
            UP -> LEFT
            LEFT -> DOWN
            DOWN -> RIGHT
            RIGHT -> UP
        }

    fun turnRight(): Direction =
        when (this) {
            UP -> RIGHT
            RIGHT -> DOWN
            DOWN -> LEFT
            LEFT -> UP
        }
}

enum class Color(val number: Int) {
    BLACK(0), WHITE(1);

    companion object {
        fun fromNumber(n: Long): Color = when (n) {
            0L -> BLACK
            else -> WHITE
        }
    }
}

data class RobotState(
    val robot: State,
    val position: Position,
    val direction: Direction,
    val panels: Map<Position, Color>
) {
    fun getCurrentColor(): Color = panels.getOrDefault(position, Color.BLACK)
}

data class Position(val x: Int, val y: Int) {
    fun neighbours(): List<Position> =
        listOf(
            copy(x = x + 1),
            copy(x = x - 1),
            copy(y = y + 1),
            copy(y = y - 1)
        )

    fun move(direction: Direction): Position =
        when (direction) {
            Direction.UP ->
                copy(y = y - 1)
            Direction.LEFT ->
                copy(x = x - 1)
            Direction.DOWN ->
                copy(y = y + 1)
            Direction.RIGHT ->
                copy(x = x + 1)
        }
}