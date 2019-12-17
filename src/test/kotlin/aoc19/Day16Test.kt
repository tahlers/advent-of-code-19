package aoc19

import io.kotlintest.data.suspend.forall
import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import io.kotlintest.tables.row
import io.vavr.collection.Vector

class Day16Test : FunSpec({

    val digitsString = this.javaClass.getResource("/day16_1.txt").readText()

    test("should produce int vector from string") {
        val result = Day16.parse("12345678")
        result shouldBe Vector.of(1, 2, 3, 4, 5, 6, 7, 8)
    }

    context("should produce phase output") {
        context("with example and different phase counts") {
            val example = Day16.parse("12345678")

            test("after 1 phase").config(enabled = false) {
                val result = Day16.fft(example, 1)
                result.joinToString("") shouldBe "48226158"
            }
            test("after 2 phase") {
                val result = Day16.fft(example, 2)
                result.joinToString("") shouldBe "34040438"
            }
            test("after 3 phase") {
                val result = Day16.fft(example, 3)
                result.joinToString("") shouldBe "03415518"
            }
            test("after 4 phase") {
                val result = Day16.fft(example, 4)
                result.joinToString("") shouldBe "01029498"
            }
        }

        test("with larger examples") {
            forall(
                row("80871224585914546619083218645595", "24176176"),
                row("19617804207202209144916044189917", "73745418"),
                row("69317163492948606335995924319873", "52432133")
            ) { input, result ->
                val r = Day16.fft(Day16.parse(input), 100)
                r.take(8).joinToString("") shouldBe result
            }
        }
    }

    test("calculation solution one") {
        val parsed = Day16.parse(digitsString)
        val result = Day16.fft(parsed, 100)
        result.take(8).joinToString("") shouldBe "90744714"

    }

    test("part2 simple example") {
        val example = Day16.parse("12345678")
        val result = Day16.fft2(example, 4, 5)
        result.joinToString("") shouldBe "498"
    }

    test("part2 example1").config {
        val text = "03036732577212944063491565474664"
        val offset = text.substring(0, 7).toInt()
        val input = Day16.parse(text.repeat(10000))
        val result = Day16.fft2(input, 100, offset)
        val message = result.take(8).joinToString("")

        message shouldBe "84462026"
    }

    test("part2 example2").config {
        val text = "02935109699940807407585447034323"
        val offset = text.substring(0, 7).toInt()
        val input = Day16.parse(text.repeat(10000))
        val result = Day16.fft2(input, 100, offset)
        val message = result.take(8).joinToString("")

        message shouldBe "78725270"
    }

    test("part2 example3").config {
        val text = "03081770884921959731165446850517"
        val offset = text.substring(0, 7).toInt()
        val input = Day16.parse(text.repeat(10000))
        val result = Day16.fft2(input, 100, offset)
        val message = result.take(8).joinToString("")

        message shouldBe "53553731"
    }

    test("calculation solution two") {
        val parsed = Day16.parse(digitsString.repeat(10000))
        val offset = digitsString.substring(0, 7).toInt()
        val result = Day16.fft2(parsed, 100, offset)
        result.take(8).joinToString("") shouldBe "82994322"

    }

})