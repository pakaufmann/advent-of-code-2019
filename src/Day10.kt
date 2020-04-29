import java.io.File
import kotlin.math.absoluteValue
import kotlin.math.min
import kotlin.math.pow

fun main(args: Array<String>) {
    val asteroids = readAsteroids("day10.txt")

    val reachableAsteroids = asteroids.map { Pair(it, ratios(it, asteroids)) }

    print("Part 1: ")
    println(reachableAsteroids.map { it.second.map { it.second }.toSet().size }.max())

    val (station, ratios) = reachableAsteroids.maxBy { it.second.map { it.second }.toSet().size }!!

    val quadrants = ratios
        .groupBy { Pair(it.second.x >= 0, it.second.y >= 0) }
        .mapValues { sortQuadrant(it.value, station) }

    val order = quadrants.getOrDefault(Pair(true, false), emptyList()) +
            quadrants.getOrDefault(Pair(true, true), emptyList()) +
            quadrants.getOrDefault(Pair(false, true), emptyList()) +
            quadrants.getOrDefault(Pair(false, false), emptyList())

    val totalOrder = IntRange(0, order.map { it.size }.max()!!).fold(emptyList<Asteroid>()) { acc, n ->
        acc + order.mapNotNull { it.getOrNull(n) }
    }

    print("Part 2: ")
    val destroyedAsteroid = totalOrder[199]
    println(destroyedAsteroid.x * 100 + destroyedAsteroid.y)
}

private fun sortQuadrant(quadrant: List<Pair<Asteroid, Ratio>>, station: Asteroid): List<List<Asteroid>> {
    return quadrant
        .groupBy { it.second }
        .mapValues { asteroidsAtRatio ->
            asteroidsAtRatio.value
                .map { asteroidAndRatio -> asteroidAndRatio.first }
                .sortedWith(compareBy { asteroid -> station.distanceTo(asteroid) })
        }
        .toSortedMap(compareByDescending { ratio -> ratio.asDouble() })
        .values
        .toList()
}

fun ratios(asteroid: Asteroid, asteroids: List<Asteroid>): List<Pair<Asteroid, Ratio>> =
    asteroids.filter { it != asteroid }
        .map { Pair(it, Ratio((it.x - asteroid.x), (it.y - asteroid.y)).minimize()) }

data class Ratio(val x: Int, val y: Int) {
    fun asDouble(): Double = x.toDouble() / y

    fun minimize(): Ratio {
        if (x == 0) {
            return Ratio(0, y / y.absoluteValue)
        }
        if (y == 0) {
            return Ratio(x / x.absoluteValue, 0)
        }

        val gcd = greatestCommonDivisor(x.absoluteValue.toLong(), y.absoluteValue.toLong()).toInt()
        return Ratio(x / gcd, y / gcd)
    }
}

data class Asteroid(val x: Int, val y: Int) {
    fun distanceTo(other: Asteroid): Int = (x - other.x).absoluteValue + (y - other.y).absoluteValue
}

fun greatestCommonDivisor(x: Long, y: Long): Long {
    val xFactors = primeFactors(x)
    val yFactors = primeFactors(y)
    return xFactors.intersect(yFactors)
        .fold(1L) { acc, n ->
            val repeated = min(xFactors.count { it == n }, yFactors.count { it == n })
            acc * n.toDouble().pow(repeated).toInt()
        }
}

tailrec fun primeFactors(n: Long, factor: Long = 2L, factors: List<Long> = emptyList()): List<Long> =
    if (n == 1L || n == 0L) {
        factors
    } else if (factor == n) {
        factors + factor
    } else if (n % factor == 0L) {
        primeFactors(n / factor, 2L, factors + factor)
    } else {
        primeFactors(n, factor + 1L, factors)
    }

fun readAsteroids(file: String): List<Asteroid> =
    File("inputs/$file")
        .readLines()
        .mapIndexed { y, line ->
            line
                .mapIndexed { x, field ->
                    when (field) {
                        '#' -> Asteroid(x, y)
                        else -> null
                    }
                }
                .flatMap { if (it == null) emptyList() else listOf(it) }
        }
        .flatten()
