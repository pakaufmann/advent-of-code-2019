import java.io.File

fun main(args: Array<String>) {
    val input = File("inputs/day8.txt").readLines().first()
    val width = 25
    val height = 6

    val layers = readLayers(input, width, height)
    val lowestZeros = layers.minBy { it.numberOf('0') }
    print("Part 1: ")
    println(lowestZeros!!.numberOf('1') * lowestZeros.numberOf('2'))

    println("Part 2: ")
    decodeImage(layers).rows.forEach { println(it.pixels) }
}

fun decodeImage(layers: List<Layer>): Layer =
    layers.reduce { top, bottom ->
        Layer(top.rows.zip(bottom.rows)
            .map {
                Row(
                    it.first.pixels.zip(it.second.pixels)
                        .map { pixels ->
                            when (pixels.first) {
                                '2' -> pixels.second
                                else -> pixels.first
                            }
                        }
                        .joinToString("")
                )
            })
    }

fun readLayers(input: String, width: Int, height: Int): List<Layer> =
    input.chunked(width * height) { layer ->
        Layer(layer.chunked(width) { Row(it.toString()) })
    }

data class Layer(val rows: List<Row>) {
    fun numberOf(char: Char): Int = rows.sumBy { it.numberOf(char) }
}

data class Row(val pixels: String) {
    fun numberOf(char: Char): Int = pixels.count { it == char }
}