import java.io.File
import kotlin.math.ceil

fun main(args: Array<String>) {
    val reactions = readReactions("day14.txt")

    print("Part 1: ")
    println(oreAmount(reactions))

    print("Part 2: ")
    println(fuelForOreAmount(reactions))
}

private fun fuelForOreAmount(reactions: List<Reaction>, amount: Long = 1000000000000): Long =
    generateSequence(Pair(1L, amount)) { range ->
        val middle = (range.first + range.second) / 2
        val oreAmount = oreAmount(reactions, middle)
        if (oreAmount > amount) {
            range.copy(second = middle)
        } else {
            range.copy(first = middle)
        }
    }
        .dropWhile { (it.second - it.first) != 1L }.first().first

private fun oreAmount(reactions: List<Reaction>, amount: Long = 1) =
    runReactions(reactions, amount).dropWhile { it.orders.isNotEmpty() }.first().oreAmount

private fun runReactions(reactions: List<Reaction>, fuelAmount: Long = 1): Sequence<Orderbook> =
    generateSequence(Orderbook(listOf(Ingredient(fuelAmount, "FUEL")), emptyMap())) { sequence ->
        val order = sequence.orders.first()
        val leftovers = sequence.leftovers.getOrDefault(order.name, 0)

        if (order.name == "ORE") {
            Orderbook(
                sequence.orders.drop(1),
                sequence.leftovers,
                sequence.oreAmount + order.amount
            )
        } else if (order.amount <= leftovers) {
            val newLeftover = leftovers - order.amount
            Orderbook(
                sequence.orders.drop(1),
                sequence.leftovers + Pair(order.name, newLeftover),
                sequence.oreAmount
            )
        } else {
            val reaction = reactions.find { it.name == order.name }!!
            val neededAmount = order.amount - leftovers
            val batches = ceil(neededAmount / reaction.amount.toDouble()).toInt()
            val newLeftovers = batches * reaction.amount - neededAmount

            Orderbook(
                sequence.orders.drop(1) + reaction.consumes.map { it.copy(amount = it.amount * batches) },
                sequence.leftovers + Pair(order.name, newLeftovers),
                sequence.oreAmount
            )
        }
    }

data class Orderbook(val orders: List<Ingredient>, val leftovers: Map<String, Long>, val oreAmount: Long = 0)

data class Ingredient(val amount: Long, val name: String)

data class Reaction(val amount: Long, val name: String, val consumes: List<Ingredient>) {
    fun consumesAll(ingredients: List<String>): Boolean =
        consumes.all { ingredients.contains(it.name) }
}

fun readReactions(file: String): List<Reaction> {
    val reactionRegex = """(.*) => ([0-9]+) ([A-Z]+)""".toRegex()

    return File("inputs/$file")
        .readLines()
        .map {
            val reaction = reactionRegex.find(it)!!.groupValues
            val ingredients = reaction[1].split(",")
                .map { ingredient ->
                    val s = ingredient.trim().split(" ")
                    Ingredient(s[0].toLong(), s[1])
                }

            Reaction(reaction[2].toLong(), reaction[3], ingredients)
        }
}
