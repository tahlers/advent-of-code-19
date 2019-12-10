package aoc19

import io.kotlintest.shouldBe
import io.kotlintest.specs.FunSpec
import io.vavr.collection.Vector

class Day9Test : FunSpec({

    context("examples") {

        test("should produce itself as output") {
            val programString = "109,1,204,-1,1001,100,1,100,1008,100,16,101,1006,101,0,99"
            val program = Day9.readProgram(programString)
            val result = Day9.runIntProgram(Day9.State(program, Vector.empty()))
            result.output.joinToString(",") shouldBe programString
        }

        test("should output 16-digit number") {
            val programString = "1102,34915192,34915192,7,4,7,99,0"
            val program = Day9.readProgram(programString)
            val result = Day9.runIntProgram(Day9.State(program, Vector.empty()))
            result.output[0].toString().length shouldBe 16
        }

        test("should output big number") {
            val programString = "104,1125899906842624,99"
            val program = Day9.readProgram(programString)
            val result = Day9.runIntProgram(Day9.State(program, Vector.empty()))
            result.output[0] shouldBe "1125899906842624"
        }

    }

    test("calculate solution one") {
        val programString = this.javaClass.getResource("/day9_1.txt").readText()
        val program = Day9.readProgram(programString)
        val result = Day9.runIntProgram(Day9.State(program, Vector.of("1")))
        result.output shouldBe Vector.of("3601950151")
    }

    test("calculate solution two") {
        val programString = this.javaClass.getResource("/day9_1.txt").readText()
        val program = Day9.readProgram(programString)
        val result = Day9.runIntProgram(Day9.State(program, Vector.of("2")))
        result.output shouldBe Vector.of("64236")
    }


})