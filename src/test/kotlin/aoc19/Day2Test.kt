package aoc19

import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row
import io.vavr.collection.Vector

class Day2Test : StringSpec({

    fun <T> Iterable<T>.toVector(): Vector<T> = Vector.ofAll(this)

    "run IntCode program" {
        forall(
            row(listOf(1,0,0,0,99), listOf(2,0,0,0,99)),
            row(listOf(2,3,0,3,99), listOf(2,3,0,6,99)),
            row(listOf(2,4,4,5,99,0), listOf(2,4,4,5,99,9801)),
            row(listOf(1,1,1,4,99,5,6,0,99), listOf(30,1,1,4,2,5,6,0,99)),
            row(listOf(1,9,10,3,2,3,11,0,99,30,40,50), listOf(3500,9,10,70,2,3,11,0, 99, 30,40,50))
        ) { input, output ->
            val result = Day2.runIntCode(input.toVector())
            result shouldBe output.toVector()
        }
    }

    "calculate solution one" {
        val program = this.javaClass.getResource("/day2_1.txt")
            .readText()
            .split(',')
            .toVector()
            .map {it.toInt()}
        val modifiedProgram = program.update(1, 12).update(2, 2)
        val resultProgram = Day2.runIntCode(modifiedProgram)
        resultProgram[0] shouldBe 4090701

    }

    "calculate solution two" {
        val program = this.javaClass.getResource("/day2_1.txt")
            .readText()
            .split(',')
            .toVector()
            .map {it.toInt()}
        val result = Day2.discoverResult(program, 19690720)
        result shouldBe 6421
    }


})