import kotlin.math.absoluteValue

fun main(args: Array<String>) {
    val start =
        "59766832516471105169175836985633322599038555617788874561522148661927081324685821180654682056538815716097295567894852186929107230155154324411726945819817338647442140954601202408433492208282774032110720183977662097053534778395687521636381457489415906710702497357756337246719713103659349031567298436163261681422438462663511427616685223080744010014937551976673341714897682634253850270219462445161703240957568807600494579282412972591613629025720312652350445062631757413159623885481128914333982571503540357043736821931054029305931122179293220911720263006705242490442826574028623201238659548887822088996956559517179003476743001815465428992906356931239533104"

    print("Part 1: ")
    println(runFFT(start).take(101).last().take(8))

    print("Part 2: ")
    val repeated = start.repeat(10000)
    val offset = repeated.take(7).toInt()

    println(runFFFT(repeated.substring(offset)).take(101).last().reversed().take(8))
}

private fun runFFFT(start: String): Sequence<String> {
    return generateSequence(start.reversed()) { number ->
        val digits = number.map { Character.getNumericValue(it) }

        var last = 0

        IntRange(0, digits.size - 1)
            .map { i ->
                val calc = (last + digits[i]) % 10
                last = calc
                calc
            }
            .joinToString("")
    }
}

private fun runFFT(start: String): Sequence<String> {

    return generateSequence(start) { number ->
        val digits = number.map { Character.getNumericValue(it) }

        IntRange(1, digits.size)
            .map { index ->
                val number = generateSequence { sequenceOf(0, 1, 0, -1) }
                    .flatten()
                    .flatMap { generateSequence { it }.take(index) }
                    .drop(1)
                    .take(number.length)
                    .zip(digits.asSequence())
                    .drop(index - 1)
                    .map { it.first * it.second }
                    .sum()

                (number % 10).absoluteValue
            }
            .joinToString("")
    }
}