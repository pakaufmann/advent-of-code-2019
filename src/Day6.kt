import java.io.File

fun main(args: Array<String>) {
    print("Part 1: ")
    val orbitMap = readOrbits("day6.txt")
    println(countOrbits(orbitMap))

    print("Part 2: ")
    val pathToYou = pathTo(orbitMap, "YOU")!!.reversed()
    val pathToSan = pathTo(orbitMap, "SAN")!!.reversed()
    val firstCommonOrbit = pathToYou.find { pathToSan.contains(it) }!!
    println(pathToYou.indexOf(firstCommonOrbit) + pathToSan.indexOf(firstCommonOrbit) - 2)
}

fun pathTo(obj: OrbitObject, search: String, path: List<String> = emptyList()): List<String>? {
    return if (obj.name == search) {
        path + obj.name
    } else {
        obj.orbitingObjects
            .map { pathTo(it, search, path + obj.name) }
            .find { it?.contains(search) ?: false }
    }
}

fun countOrbits(orbits: OrbitObject, depth: Int = 0): Int {
    return if (orbits.orbitingObjects.isEmpty()) {
        depth
    } else {
        depth + orbits.orbitingObjects.sumBy { countOrbits(it, depth + 1) }
    }
}

fun readOrbits(file: String): OrbitObject {
    val orbits = File("inputs/$file")
        .readLines()
        .map {
            val splitted = it.split(")")
            Pair(splitted.first(), splitted.last())
        }

    fun buildOrbits(obj: String): OrbitObject =
        OrbitObject(
            obj,
            orbits
                .filter { it.first == obj }
                .map { buildOrbits(it.second) }
        )

    return buildOrbits("COM")
}

data class OrbitObject(val name: String, val orbitingObjects: List<OrbitObject>)