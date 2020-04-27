import java.io.File
import java.lang.IllegalArgumentException
import kotlin.math.absoluteValue

fun main(args: Array<String>) {
    val directions = readDirections("day3.txt")
        .map { buildPath(it) }

    val directionSet = directions.map { it.toSet() }

    val intersections = directionSet
        .flatten()
        .filter { c -> directionSet.all { it.contains(c) } }

    print("Part 1: ")
    println(intersections.map { it.x.absoluteValue + it.y.absoluteValue }.min())

    val minDistance = intersections.map { intersection ->
        directions.map { path -> path.indexOf(intersection) }.sum()
    }.min()!!
    
    print("Part 2: ")
    println(minDistance + 2)
}

fun buildPath(directions: List<String>): List<Coordinate> {
    return directions.fold(Pair(emptyList<Coordinate>(), Coordinate(0, 0)), { state, direction ->
        when (direction.first()) {
            'U' -> {
                val (newPosition, path) = state.second.up(direction.drop(1).toInt())
                Pair(state.first + path, newPosition)
            }
            'D' -> {
                val (newPosition, path) = state.second.down(direction.drop(1).toInt())
                Pair(state.first + path, newPosition)
            }
            'L' -> {
                val (newPosition, path) = state.second.left(direction.drop(1).toInt())
                Pair(state.first + path, newPosition)
            }
            'R' -> {
                val (newPosition, path) = state.second.right(direction.drop(1).toInt())
                Pair(state.first + path, newPosition)
            }
            else -> throw IllegalArgumentException("invalid state")
        }
    }).first
}

data class Coordinate(val x: Int, val y: Int) {
    fun up(steps: Int): Pair<Coordinate, List<Coordinate>> =
        Pair(
            copy(y = y + steps),
            IntRange(1, steps).map { copy(y = y + it) }
        )

    fun down(steps: Int): Pair<Coordinate, List<Coordinate>> =
        Pair(
            copy(y = y - steps),
            IntRange(1, steps).map { copy(y = y - it) }
        )

    fun left(steps: Int): Pair<Coordinate, List<Coordinate>> =
        Pair(
            copy(x = x - steps),
            IntRange(1, steps).map { copy(x = x - it) }
        )

    fun right(steps: Int): Pair<Coordinate, List<Coordinate>> =
        Pair(
            copy(x = x + steps),
            IntRange(1, steps).map { copy(x = x + it) }
        )

}

fun readDirections(file: String) =
    File("inputs/$file")
        .readLines()
        .map { it.split(",") }
