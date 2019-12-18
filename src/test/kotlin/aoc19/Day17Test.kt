package aoc19

import aoc19.Day17.State
import io.kotlintest.data.suspend.forall
import io.kotlintest.shouldBe
import io.kotlintest.shouldNotBe
import io.kotlintest.specs.FunSpec
import io.kotlintest.tables.row
import io.vavr.collection.Vector

class Day17Test : FunSpec({

    val program = Day15.readProgram(this.javaClass.getResource("/day17_1.txt").readText())

    test("should produce image") {
        val result = Day17.getImage(State(program))
        result.isEmpty shouldNotBe true
    }

    test("should calculate solution one") {
        val result = Day17.getAlignmentParam(State(program))
        result shouldBe 1544
    }

    test("should calculate solution two") {
        val result = Day17.reportDust(State(program))
        result shouldBe "696373"
    }

})