package aoc19

import io.vavr.collection.Stream
import io.vavr.collection.Vector
import kotlin.math.abs

object Day16 {

    fun parse(input: String): Vector<Int> {
        return Vector.ofAll(input.map { it.toInt() - 48 })
    }

    private fun phasePattern(n: Int, length: Int): Vector<Int> {
        val init = Stream.of(0, 1, 0, -1).flatMap { Stream.of(it).cycle(n) }
        return init.cycle().tail().take(length).toVector()
    }

    fun fft(input: Vector<Int>, count: Int): Vector<Int> {
        val length = input.length()
        val output = (1..count).fold(input) { newInput, _ ->
            val newOutput = (0 until length).map { index ->
                val phasePattern = phasePattern(index + 1, input.length())
                val newDigit = abs(newInput.zipWith(phasePattern) { d, pattern ->
                    d * pattern
                }.sum().toInt()) % 10
                newDigit
            }
            newOutput.toVector()
        }
        return output
    }

    fun fft2(input: Vector<Int>, count: Int, offset: Int): Vector<Int> {
        val offsetVector = input.subSequence(offset)
        val output = (1..count).fold(offsetVector) { newInput, _ ->
            val newOutput = newInput.reverse().fold(Vector.empty<Int>()) { vec, current ->
                val newDigit = if (vec.isEmpty) current else vec.head() + current
                vec.prepend(newDigit % 10)
            }
            newOutput
        }
        return output
    }
}