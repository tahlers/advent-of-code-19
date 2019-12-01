package aoc19

import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row

class Day1Test : StringSpec({

    "calculate fuel for single module" {
        forall(
            row(12, 2),
            row(14, 2),
            row(1969, 654),
            row(100756, 33583)
        ) { weight, fuel ->
            Day1.calculateFuel(weight) shouldBe fuel
        }
    }

    "calculate solution one" {
        val lines = this.javaClass.getResource("/day1_1.txt").readText().lines()
        val fuel = Day1.calculateFuelByModuleWeightStrings(lines)
        fuel shouldBe 3269199
    }

    "calculate progressive fuel" {
        forall(
            row(14, 2),
            row(1969, 966),
            row(100756, 50346)
        ) { weight, fuel ->
            Day1.calculateFuel2(weight) shouldBe fuel
        }
    }

    "calculate solution two" {
        val lines = this.javaClass.getResource("/day1_1.txt").readText().lines()
        val fuel = Day1.calculateFuel2ByModuleWeightStrings(lines)
        fuel shouldBe 4900909
    }


})