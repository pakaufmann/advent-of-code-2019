import java.io.File
import java.lang.IllegalArgumentException
import kotlin.math.absoluteValue
import kotlin.math.*

fun main(args: Array<String>) {
    val shuffleOrder = readShuffling("day22.txt")

    val initial = generateSequence(0) { it + 1 }

    val res = shuffleOrder.fold(initial.take(10007).toList()) { deck, shuffle ->
        shuffleDeck(deck, shuffle)
    }

    print("Part 1: ")
    println(res.indexOf(2019))

    print("Part 2: ")
    println(solve(shuffleOrder, 2020, 119315717514047, -101741582076661))
}

infix fun Long.modulo(mod: Long) = (this % mod).let { (it shr Long.SIZE_BITS - 1 and mod) + it }

/**
 * x - input number
 * n - number of cards
 * k - iterations, positive = get position of card x, negative = get card in position x
 */
fun solve(instructions: List<Shuffle>, x: Long, m: Long, k: Long): Long {
    // compose basis function
    // f(x) = ax + b
    var a = 1L
    var b = 0L

    for (ln in instructions) {
        when (ln) {
            is NewStack -> {
                // x → -x - 1; ax + b → -ax - b - 1
                a = -a modulo m
                b = b.inv() modulo m // b.inv() = -b - 1
            }
            is Cut -> {
                // x → x - i; ax + b → ax + b - i
                b = b - ln.number modulo m
            }
            is Increment -> {
                // x → x · i; ax + b → aix + bi
                a = a.mulMod(ln.inc, m)
                b = b.mulMod(ln.inc, m)
            }
            else -> error("Unrecognized instruction: $ln")
        }
    }

    // invert basis function. f^-1(x) = (a^-1)(x - b)
    if (k < 0) {
        a = a.powMod(m - 2, m) // modular multiplicative inverse for prime m
        b = a.mulMod(-b, m)
    }

    // start exponentiation for function, f^k(x) = cx + d
    var c = 1L
    var d = 0L
    var e = abs(k)

    // exponentiation by squaring. Equivalent to computing
    // ⌈ a 0 ⌉ k
    // ⌊ b 1 ⌋
    while (e > 0) {
        if (e and 1 == 1L) {
            // a(cx + d) + b = acx + (ad + b)
            c = a.mulMod(c, m)
            d = (a.mulMod(d, m) + b) % m
        }
        e = e shr 1
        b = (a.mulMod(b, m) + b) % m
        a = a.mulMod(a, m)
    }

    return (x.mulMod(c, m) + d) % m
}

fun Long.mulMod(other: Long, m: Long): Long = _mulMod(this modulo m, other modulo m, m)

// unchecked version, assumes 0 <= a, b < m
fun _mulMod(a: Long, b: Long, m: Long): Long {
    if (m <= FLOOR_SQRT_MAX_LONG + 1) return a * b % m
    if (m < 1L shl 57) {
        val g = a.toDouble() * b / m
        return a * b - g.toLong() * m modulo m
    }
    if (m > Long.MIN_VALUE ushr 1) return _doubleAndAdd(a, b, m)
    var hi = Math.multiplyHigh(a, b) shl 1
    var lo = a * b
    if (lo < 0) {
        hi = hi or 1
        lo = lo xor Long.MIN_VALUE
    }

    return _norm(hi.shl63Mod(m) + lo % m - m, m)
}

// normalizes an integer that's within range [-MOD, MOD) without branching
inline fun _norm(it: Long, mod: Long) = (it shr Long.SIZE_BITS - 1 and mod) + it
private fun _doubleAndAdd(a: Long, b: Long, m: Long): Long {
    var res = 0L
    var b = b
    var a = a
    if (a < b) a = b.also { b = a }

    while (b > 0) {
        if (b and 1 == 1L) {
            res = _norm(res + a - m, m)
        }
        b = b shr 1
        a = _norm(a.shl(1) - m, m)
    }
    return res
}

const val FLOOR_SQRT_MAX_LONG = 3037000499L
inline val Long.numLeadingZeroes get() = java.lang.Long.numberOfLeadingZeros(this)
private fun Long.shl63Mod(m: Long): Long {
    // assumes 0 <= a < m <= 2^62
    var a = this
    var remShift = 63
    do {
        val shift = min(remShift, a.numLeadingZeroes - 1)
        a = a.shl(shift) % m
        remShift -= shift
    } while (remShift > 0)

    return a
}

fun Long.powMod(exponent: Long, mod: Long): Long {
    if (exponent < 0) error("Inverse not implemented")
    var res = 1L
    var e = exponent
    var b = modulo(mod)

    while (e > 0) {
        if (e and 1 == 1L) {
            res = _mulMod(res, b, mod)
        }
        e = e shr 1
        b = _mulMod(b, b, mod)
    }
    return res
}

fun shuffleDeck(deck: List<Int>, shuffle: Shuffle): List<Int> =
    when (shuffle) {
        is NewStack ->
            deck.reversed()
        is Cut ->
            if (shuffle.number < 0) {
                deck.takeLast(shuffle.number.absoluteValue.toInt()) + deck.dropLast(shuffle.number.absoluteValue.toInt())
            } else {
                deck.drop(shuffle.number.toInt()) + deck.take(shuffle.number.toInt())
            }
        is Increment ->
            deck
                .fold(Pair(0, arrayOfNulls<Int>(deck.size))) { acc, n ->
                    val (pos, arr) = acc
                    arr[pos] = n
                    Pair((pos + shuffle.inc.toInt()) % deck.size, arr)
                }
                .second
                .filterNotNull()
        else -> throw IllegalArgumentException("Unkown shuffle command")
    }

interface Shuffle

object NewStack : Shuffle

data class Cut(val number: Long) : Shuffle

data class Increment(val inc: Long) : Shuffle

fun readShuffling(file: String): List<Shuffle> =
    File("inputs/$file")
        .readLines()
        .map {
            when {
                it.startsWith("cut") ->
                    Cut(it.split(" ")[1].toLong())
                it.startsWith("deal with increment") ->
                    Increment(it.split(" ").last().toLong())
                else ->
                    NewStack
            }
        }
