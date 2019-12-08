package aoc19

import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import io.vavr.collection.Vector
import kotlin.streams.toList

class Day8Test : FunSpec({

    context("sample data") {

        val digits = "123456789012".chars().map(Character::getNumericValue).toList().toVector()

        test("should parse digits into layers") {

            Day8.parseImage(digits, 3, 2) shouldBe Vector.of(
                Vector.of(
                    Vector.of(1, 2, 3),
                    Vector.of(4, 5, 6)
                ),
                Vector.of(
                    Vector.of(7, 8, 9),
                    Vector.of(0, 1, 2)
                )
            )
        }

        test("should find fewest 0 digit layer checksum") {
            val layers = Day8.parseImage(digits, 3, 2)
            Day8.findFewest0DigitsChecksum(layers) shouldBe 1
        }
    }


    test("calculate solution one") {
        val digitsString = this.javaClass.getResource("/day8_1.txt").readText()
        val digits = digitsString.chars().map(Character::getNumericValue).toList().toVector()
        val layers = Day8.parseImage(digits, 25, 6)
        Day8.findFewest0DigitsChecksum(layers) shouldBe 1215
    }

    test("should produce picture") {
        val digits = "0222112222120000".chars().map(Character::getNumericValue).toList().toVector()
        val layers = Day8.parseImage(digits, 2, 2)
        Day8.producePicture(layers) shouldBe Vector.of(
            Vector.of(0, 1),
            Vector.of(1, 0)
        )
    }

    test("calculate solution two") {
        val digitsString = this.javaClass.getResource("/day8_1.txt").readText()
        val digits = digitsString.chars().map(Character::getNumericValue).toList().toVector()
        val layers = Day8.parseImage(digits, 25, 6)
        Day8.producePicture(layers).size() shouldBe 6
    }

})