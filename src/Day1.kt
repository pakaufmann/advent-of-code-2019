import java.io.File

fun main(args: Array<String>) {
    val lines = readMasses("day1.txt")
    print("Part 1: ")
    println(totalFuel(lines))

    print("Part 2: ")
    println(totalFuelRecursive(lines))
}

fun totalFuel(masses: List<Int>) = masses.map { calculateFuel(it) }.sum()

fun totalFuelRecursive(masses: List<Int>) = masses.map { calculateFuelRecursive(it) }.sum()

fun calculateFuelRecursive(mass: Int) =
    generateSequence(calculateFuel(mass)) { calculateFuel(it) }.takeWhile { it > 0 }.sum()

fun calculateFuel(mass: Int) = (mass / 3) - 2

fun readMasses(file: String) = File("inputs/$file").readLines().map { it.toInt() }