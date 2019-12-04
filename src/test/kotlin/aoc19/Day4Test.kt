package aoc19

import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row

class Day4Test : StringSpec({

    "isValid should return true for valid responses "  {
        forall(
            row("111111", true),
            row("223450", false),
            row("223458", true),
            row("123788", true),
            row("123789", false)
        ) { input, expected ->
            Day4.isValid(input) shouldBe expected
        }
    }


    "calculate solution one" {
        Day4.numberOfValidCombinations("156218", "652527") shouldBe 1694
    }

    "isValid2 should return true for valid responses " {
        forall(
            row("111111", false),
            row("112233", true),
            row("123444", false),
            row("222444", false),
            row("111122", true),
            row("223450", false),
            row("223458", true),
            row("123788", true),
            row("123789", false)
        ) { input, expected ->
            Day4.isValid2(input) shouldBe expected
        }
    }

    "calculate solution two" {
        Day4.numberOfValidCombinations2("156218", "652527") shouldBe 1148
    }

})