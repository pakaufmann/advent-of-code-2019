fun main(args: Array<String>) {
    val input = IntRange(356261, 846303)

    print("Part 1: ")
    println(input.count { matchesPart1(it) })

    print("Part 2: ")
    println(input.count { matchesPart1(it) && matchesPart2(it) })
}

fun matchesPart2(password: Int): Boolean {
    val digits = listOf(1_000_000, 100_000, 10_000, 1000, 100, 10)
        .map { password % it / (it / 10) }

    val repeated = digits.windowed(2).filter { it.first() == it.last() }.map { it.first() }.toSet()

    return repeated.map { it + it * 10 + it * 100 }.any { !password.toString().contains(it.toString()) }
}

fun matchesPart1(password: Int): Boolean {
    val digits = listOf(1_000_000, 100_000, 10_000, 1000, 100, 10)
        .map { password % it / (it / 10) }

    return digits.windowed(2).any { it.first() == it.last() } &&
            digits.sorted() == digits
}