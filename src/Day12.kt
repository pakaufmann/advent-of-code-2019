import java.io.File
import kotlin.math.absoluteValue
import kotlin.math.max
import kotlin.math.pow

fun main(args: Array<String>) {
    val initialMoons = readMoons("day12.txt")

    val result = runSimulation(initialMoons).drop(1000).first()

    val xAxises = initialMoons.map { Pair(it.position.x, it.velocity.x) }
    val yAxises = initialMoons.map { Pair(it.position.y, it.velocity.y) }
    val zAxises = initialMoons.map { Pair(it.position.z, it.velocity.z) }

    print("Part 1: ")
    println(calculateEnergy(result))

    print("Part 2: ")
    val loopX = findLoop(xAxises)
    val loopY = findLoop(yAxises)
    val loopZ = findLoop(zAxises)

    println(leastCommonMultiple(leastCommonMultiple(loopX, loopY), loopZ))
}

fun leastCommonMultiple(x: Long, y: Long): Long {
    val xFactors = primeFactors(x)
    val yFactors = primeFactors(y)
    return (xFactors.toSet() + yFactors)
        .fold(1L) { acc, n ->
            val repeated = max(xFactors.count { it == n }, yFactors.count { it == n })
            acc * n.toDouble().pow(repeated).toInt()
        }
}

private fun findLoop(axis: List<Pair<Int, Int>>): Long {
    // immutable version is possible, but way to slow to be usable in kotlin, as
    // sets are copied completely, instead of using changesets
    val foundSets = mutableSetOf<List<Pair<Int, Int>>>()
    var lastFound = axis

    while (!foundSets.contains(lastFound)) {
        foundSets.add(lastFound)
        lastFound = runSimulationSingleAxis(lastFound).take(2).last()
    }

    return foundSets.size.toLong()
}

private fun calculateEnergy(moons: List<Moon>): Int = moons.map { it.totalEnergy() }.sum()

private fun runSimulationSingleAxis(initial: List<Pair<Int, Int>>) =
    generateSequence(initial) { axises ->
        axises.map { axis ->
            val newVelocity = axises
                .fold(axis.second) { velocity, other ->
                    velocity + delta(axis.first, other.first)
                }
            Pair(axis.first + newVelocity, newVelocity)
        }
    }

private fun runSimulation(initalMoons: List<Moon>): Sequence<List<Moon>> =
    generateSequence(initalMoons) { moons ->
        moons.map { moon ->
            val newVelocity = moons
                .filter { it != moon }
                .fold(moon.velocity) { velocity, other ->
                    Velocity(
                        velocity.x + delta(moon.position.x, other.position.x),
                        velocity.y + delta(moon.position.y, other.position.y),
                        velocity.z + delta(moon.position.z, other.position.z)
                    )
                }

            moon.update(newVelocity)
        }
    }

fun delta(f: Int, s: Int): Int =
    if (f > s) {
        -1
    } else if (f < s) {
        1
    } else {
        0
    }

fun readMoons(file: String): List<Moon> {
    val regex = """<x=([\-0-9]+), y=([-0-9]+), z=([-0-9]+)>""".toRegex()

    return File("inputs/$file").readLines()
        .map { l ->
            val result = regex.find(l)!!.groups

            Moon(
                Pos3D(
                    result[1]!!.value.toInt(),
                    result[2]!!.value.toInt(),
                    result[3]!!.value.toInt()
                ),
                Velocity(0, 0, 0)
            )
        }
}

data class Pos3D(val x: Int, val y: Int, val z: Int) {
    fun update(newVelocity: Velocity): Pos3D =
        Pos3D(x + newVelocity.x, y + newVelocity.y, z + newVelocity.z)

    fun energy(): Int = x.absoluteValue + y.absoluteValue + z.absoluteValue
}

data class Velocity(val x: Int, val y: Int, val z: Int) {
    fun energy(): Int = x.absoluteValue + y.absoluteValue + z.absoluteValue
}

data class Moon(val position: Pos3D, val velocity: Velocity) {
    fun update(newVelocity: Velocity): Moon = Moon(position.update(newVelocity), newVelocity)

    fun totalEnergy(): Int = position.energy() * velocity.energy()
}