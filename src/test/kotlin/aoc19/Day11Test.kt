package aoc19

import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.vavr.collection.HashMap

class Day11Test : FreeSpec({

    val program = Day11.readProgram(this.javaClass.getResource("/day11_1.txt").readText())

    "calculate solution one" - {
        val state = Day11.State(program)
        val result = Day11.paintJob(state, HashMap.empty(), Day11.Pos(0, 0), Day11.Color.BLACK, Day11.Direction.UP)
        val result2 = Day11.paintJob(state, HashMap.empty(), Day11.Pos(0, 0), Day11.Color.WHITE, Day11.Direction.UP)
        Day11.printGrid(result2)
        result.size() shouldBe 1771 // 2948 not correct, 1826 not correct
    }


})