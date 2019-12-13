package aoc19

import aoc19.Day13.TileType.BLOCK
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class Day13Test : FreeSpec({

    val program = Day13.readProgram(this.javaClass.getResource("/day13_1.txt").readText())

    "calculate solution one" - {

        val screen = Day13.computeWorld(program)
        val result = screen.filter { it.type == BLOCK }
        result.size() shouldBe 315

    }

    "calculate solution two" - {
        val updatedProgram = program.put(0L, "2")
        val score = Day13.finalScore(updatedProgram)
        score shouldBe 16171

    }

    "just play".config(enabled = false) {
        val updatedProgram = program.put(0L, "2")

        Day13.playWithTerminal(updatedProgram)
    }

})