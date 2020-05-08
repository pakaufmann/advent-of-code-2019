import java.io.File
import java.lang.IllegalArgumentException

fun main(args: Array<String>) {
    val (vault, entrance) = readVault("day18.txt")

    print("Part 1: ")
    println(solve(vault, entrance))

    print("Part 2: ")
    println(vault.splitAt(entrance).sumBy { solve(it.first, it.second)!! })
}

private fun solve(grid: Vault, entrance: Position): Int? {
    val allKeys = grid.getKeys()
    val unopenableDoors = grid.getDoors().filter { !allKeys.containsValue(Key(it.value.name)) }

    val clearedGrid = grid.clearPositions(unopenableDoors.map { it.key })

    val keys = clearedGrid.getKeys().map { Pair(it.key, clearedGrid.reachableFrom(it.key)) }.toMap()
    val doors = clearedGrid.getDoors().map { Pair(it.key, clearedGrid.reachableFrom(it.key)) }.toMap()

    val keyNames = allKeys.values.map { it.name }.toSet()

    cache.clear()
    return distanceToCollectKeys(entrance, keyNames, keyNames, allKeys, keys + doors)
}

val cache = mutableMapOf<Pair<Position, Set<String>>, Int>()

fun distanceToCollectKeys(
    currentPosition: Position,
    allKeys: Set<String>,
    keysToCollect: Set<String>,
    keys: Map<Position, Key>,
    keysAndDoors: Map<Position, VaultSearchState>
): Int {
    if (keysToCollect.isEmpty()) {
        return 0
    }

    val cacheKey = Pair(currentPosition, keysToCollect)
    val cached = cache[cacheKey]
    if (cached != null) {
        return cached
    }

    val result = nextReachableKeys(currentPosition, keys, keysAndDoors, allKeys - keysToCollect)
        .map { reachableKey ->
            reachableKey.value.steps + distanceToCollectKeys(
                reachableKey.value.position,
                allKeys,
                keysToCollect - reachableKey.key,
                keys,
                keysAndDoors
            )
        }
        .min() ?: 0

    cache[cacheKey] = result
    return result
}

data class Step(val position: Position, val steps: Int)

data class ReachableState(
    val toVisit: List<Step>,
    val visited: Map<Position, Int>,
    val foundKeys: Map<String, Step>
)

fun nextReachableKeys(
    position: Position,
    keys: Map<Position, Key>,
    keysAndDoors: Map<Position, VaultSearchState>,
    holdingKeys: Set<String>,
    steps: Int = 0
): Map<String, Step> {
    return generateSequence(
        ReachableState(listOf(Step(position, steps)), emptyMap(), emptyMap())
    ) { updateReachableState(it, keysAndDoors, holdingKeys, keys) }
        .dropWhile { it.toVisit.isNotEmpty() }
        .first()
        .foundKeys
}

private fun updateReachableState(
    state: ReachableState,
    keysAndDoors: Map<Position, VaultSearchState>,
    heldKeys: Set<String>,
    keys: Map<Position, Key>
): ReachableState {
    val (toVisit, visited, reachableKeys) = state

    val next = toVisit.first()

    val alreadyVisited = visited[next.position]

    if (alreadyVisited != null && alreadyVisited <= next.steps) {
        return state.copy(toVisit = toVisit.drop(1))
    }

    val reachableFromHere = keysAndDoors[next.position]!!

    val passableDoors = reachableFromHere.reachableDoors
        .filter { heldKeys.contains(it.key) }
        .map { key -> Step(key.value.first, key.value.second + next.steps) }
    val possibleKeys = reachableFromHere.reachableKeys
        .map { key -> Step(key.value.first, key.value.second + next.steps) }

    val allPossibilities = possibleKeys + passableDoors

    val validPossibilities = allPossibilities.filter { reachable ->
        visited[reachable.position].let { it == null || it > reachable.steps }
    }

    val newReachableKeys = checkKeys(keys[next.position], reachableKeys, heldKeys, next)

    return ReachableState(
        toVisit.drop(1) + validPossibilities,
        visited + Pair(next.position, next.steps),
        newReachableKeys
    )
}

private fun checkKeys(
    key: Key?,
    reachableKeys: Map<String, Step>,
    heldKeys: Set<String>,
    next: Step
): Map<String, Step> {
    if (key == null) {
        return reachableKeys
    }

    if (heldKeys.contains(key.name)) {
        return reachableKeys
    }

    val alreadyReachable = reachableKeys[key.name]
    return if (alreadyReachable != null && alreadyReachable.steps < next.steps) {
        reachableKeys
    } else {
        reachableKeys + Pair(key.name, Step(next.position, next.steps))
    }
}

interface VaultType

object Floor : VaultType

data class Door(var name: String) : VaultType

data class Key(var name: String) : VaultType

data class VaultSearch(val position: Position, val steps: Int = 0)

data class VaultSearchState(
    val toSearch: List<VaultSearch>,
    val visited: Set<Position> = emptySet(),
    val reachableKeys: Map<String, Pair<Position, Int>> = emptyMap(),
    val reachableDoors: Map<String, Pair<Position, Int>> = emptyMap()
) {
    fun next(): VaultSearch = toSearch.first()

    fun update(toVisit: List<VaultSearch>, visited: Position): VaultSearchState =
        copy(
            toSearch = toSearch.drop(1) + toVisit,
            visited = this.visited + visited
        )
}

data class Vault(val map: Map<Position, VaultType>) {
    fun reachableFrom(position: Position): VaultSearchState {
        return generateSequence(VaultSearchState(listOf(VaultSearch(position)))) { state ->
            val next = state.next()
            if (next.position == position) {
                addNeighbours(next, state)
            } else {
                when (val mapType = map[next.position]) {
                    is Floor -> addNeighbours(next, state)
                    is Key ->
                        addNeighbours(
                            next,
                            state.copy(
                                reachableKeys = state.reachableKeys + Pair(
                                    mapType.name,
                                    Pair(next.position, next.steps)
                                )
                            )
                        )
                    is Door ->
                        state.copy(
                            reachableDoors = state.reachableDoors + Pair(
                                mapType.name,
                                Pair(next.position, next.steps)
                            )
                        ).update(emptyList(), next.position)
                    else -> throw IllegalArgumentException("not possible")
                }
            }
        }
            .dropWhile { it.toSearch.isNotEmpty() }
            .first()
    }

    private fun addNeighbours(next: VaultSearch, state: VaultSearchState): VaultSearchState {
        val possibleNeighbours = next.position.neighbours()

        val toVisit = possibleNeighbours
            .filter { !state.visited.contains(it) && map.containsKey(it) }
            .map { VaultSearch(it, next.steps + 1) }

        return state.update(toVisit, next.position)
    }

    fun splitAt(split: Position): List<Pair<Vault, Position>> =
        listOf(
            split({ it < split.x }, { it < split.y }, Position(split.x - 1, split.y - 1)),
            split({ it > split.x }, { it < split.y }, Position(split.x + 1, split.y - 1)),
            split({ it < split.x }, { it > split.y }, Position(split.x - 1, split.y + 1)),
            split({ it > split.x }, { it > split.y }, Position(split.x + 1, split.y + 1))
        )

    private fun split(filterX: (Int) -> Boolean, filterY: (Int) -> Boolean, newStart: Position): Pair<Vault, Position> {
        return Pair(
            Vault(map.filter { filterX(it.key.x) && filterY(it.key.y) } + Pair(newStart, Key("@"))),
            newStart
        )
    }

    fun clearPositions(toClear: List<Position>): Vault =
        Vault(map.mapValues {
            if (toClear.contains(it.key)) {
                Floor
            } else {
                it.value
            }
        })

    fun getKeys(): Map<Position, Key> = map.filterValues { it is Key }.mapValues { it.value as Key }

    fun getDoors(): Map<Position, Door> = map.filterValues { it is Door }.mapValues { it.value as Door }
}

fun readVault(file: String): Pair<Vault, Position> {
    val input = File("inputs/$file")
        .readLines()
        .mapIndexed { y, line ->
            line.mapIndexed { x, char ->
                Pair(Position(x, y), char)
            }
        }
        .flatten()
        .toMap()

    return Pair(
        Vault(input.filter { it.value != '#' }.mapValues {
            when {
                it.value == '.' -> Floor
                it.value.isUpperCase() -> Door(it.value.toString().toLowerCase())
                it.value.isLowerCase() || it.value == '@' ->
                    Key(it.value.toString())
                else -> throw IllegalArgumentException("unknown char: ${it.value}")
            }
        }),
        input.filterValues { it == '@' }.keys.first()
    )
}
