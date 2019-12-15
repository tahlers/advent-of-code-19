package aoc19


import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class Day15Test : FreeSpec({

    val program = Day15.readProgram(this.javaClass.getResource("/day15_1.txt").readText())

    "calculate solution one" - {
        val result = Day15.countMoves(program)
        result shouldBe 224
    }

    "calculate solution two" - {
        val result = Day15.calcOxygenFill(program)
        result shouldBe 284
    }

})