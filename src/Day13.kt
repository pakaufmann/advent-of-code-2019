import java.lang.IllegalArgumentException

fun main(args: Array<String>) {
    val arcade = State(0, readProgram("day13.txt"))
    val game = runGame(arcade)

    print("Part 1: ")
    println(game.count { it.second.type == TileType.BLOCK })

    println("Part 2: ")

    val run = generateSequence(GameState(arcade.writeValue(0, 2L), null, null, 0)) {
        val nextState = runGame(it.game.copy(output = null), sequenceOf(it.joystick))
            .dropWhile {
                it.second.type != TileType.BALL &&
                        it.second.type != TileType.PADDLE &&
                        it.second.type != TileType.HIGHSCORE &&
                        !it.first.finished()
            }

        it.updateState(nextState)
    }

    println(run
        .dropWhile { !it.finished }
        .first()
        .highscore!!
        .y)
}

data class GameState(
    val game: State,
    val paddle: Tile?,
    val highscore: Tile?,
    val joystick: Long,
    val finished: Boolean = false
) {
    fun updateState(next: Sequence<Pair<State, Tile>>): GameState {
        if (next.none()) {
            return copy(finished = true)
        }

        val (nextState, currentTile) = next.first()

        if (currentTile.type == TileType.HIGHSCORE) {
            return GameState(nextState, paddle, currentTile, joystick)
        }

        val move = moveJoystick(currentTile)

        val paddle = if (currentTile.type == TileType.PADDLE) currentTile else paddle
        return GameState(nextState, paddle, highscore, move)
    }

    private fun moveJoystick(currentTile: Tile): Long {
        if (paddle == null) {
            return 0
        }

        if (currentTile.type == TileType.BALL) {
            return when {
                paddle.x < currentTile.x -> 1L
                paddle.x > currentTile.x -> -1L
                else -> 0
            }
        }

        return joystick
    }
}

private fun runGame(arcade: State, inputList: Sequence<Long> = emptySequence()): Sequence<Pair<State, Tile>> {
    return runProgram(arcade, inputList)
        .filter { it.output != null }
        .windowed(3, 3)
        .map { Pair(it.last(), Tile.fromList(it.mapNotNull { it.output })) }
}

enum class TileType {
    EMPTY, WALL, BLOCK, PADDLE, BALL, HIGHSCORE;

    companion object {
        fun fromNumber(n: Int) = when (n) {
            0 -> EMPTY
            1 -> WALL
            2 -> BLOCK
            3 -> PADDLE
            4 -> BALL
            5 -> HIGHSCORE
            else -> throw IllegalArgumentException("invalid number $n")
        }
    }
}

data class Tile(val x: Int, val y: Int, val type: TileType) {
    companion object {
        fun fromList(l: List<Long>): Tile {
            if (l.first() == -1L) {
                return Tile(-1, l.last().toInt(), TileType.HIGHSCORE)
            }
            return Tile(l.first().toInt(), l[1].toInt(), TileType.fromNumber(l.last().toInt()))
        }
    }
}