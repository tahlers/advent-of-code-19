package aoc19

import io.kotlintest.data.forall
import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.kotlintest.tables.row
import io.vavr.collection.Vector

import aoc19.Day5.Instruction
import aoc19.Day5.Opcode
import aoc19.Day5.ParamMode.*
import aoc19.Day5.Param

class Day5Test : StringSpec({

    fun <T> Iterable<T>.toVector(): Vector<T> = Vector.ofAll(this)

    "should be able to read Instructions from program" {
        forall(
            row("99", 0, Instruction(Opcode.HALT)),
            row("99,3,50", 1, Instruction(Opcode.INPUT, listOf(Param(50, POSITION)))),
            row("99,4,50", 1, Instruction(Opcode.OUTPUT, listOf(Param(50, POSITION)))),
            row("1002,4,3,4,33", 0, Instruction(Opcode.MUL, listOf(
                Param(4, POSITION), Param(3, IMMEDIATE), Param(4, POSITION)))),
            row("1002,4,3,4,99", 4, Instruction(Opcode.HALT))
        ) { program, offset, result ->
            val parsedProgram = program.split(',').toVector()
            Day5.readInstruction(parsedProgram, offset) shouldBe result
        }
    }

    "should be able to run programs"{
        forall(
            row("1002,4,3,4,33", "", "1002,4,3,4,99"),
            row("1101,100,-1,4,0", "", "1101,100,-1,4,99"),
            row("3,0,4,0,99", "69", "69,0,4,0,99")
        ) { program, input, result ->
            val parsedProgram = program.split(',').toVector()
            val state = Day5.State(parsedProgram, input)
            Day5.runIntProgram(state).program.joinToString(",") shouldBe result
        }
    }

    "calculate solution one" {
        val program = this.javaClass.getResource("/day5_1.txt")
            .readText()
            .split(',')
            .toVector()
        val state = Day5.State(program, "1")
        Day5.runIntProgram(state).output.joinToString(",") shouldBe "0,0,0,0,0,0,0,0,0,15259545"
    }

    "should be able to run with new opcodes" {
        forall(
            row("3,9,8,9,10,9,4,9,99,-1,8", "7", "0"),
            row("3,9,8,9,10,9,4,9,99,-1,8", "8", "1"),
            row("3,9,7,9,10,9,4,9,99,-1,8", "1", "1"),
            row("3,9,7,9,10,9,4,9,99,-1,8", "9", "0"),
            row("3,3,1108,-1,8,3,4,3,99", "7", "0"),
            row("3,3,1108,-1,8,3,4,3,99", "8", "1"),
            row("3,3,1107,-1,8,3,4,3,99", "1", "1"),
            row("3,3,1107,-1,8,3,4,3,99", "9", "0"),
            row("3,12,6,12,15,1,13,14,13,4,13,99,-1,0,1,9", "0", "0"),
            row("3,12,6,12,15,1,13,14,13,4,13,99,-1,0,1,9", "10", "1"),
            row("3,3,1105,-1,9,1101,0,0,12,4,12,99,1", "0", "0"),
            row("3,3,1105,-1,9,1101,0,0,12,4,12,99,1", "11", "1")
        ) {program, input, result ->
            val parsedProgram = program.split(',').toVector()
            val state = Day5.State(parsedProgram, input)
            val resultState = Day5.runIntProgram(state)
            resultState.output.joinToString(",") shouldBe result
        }
    }

    "calculate solution two" {
        val program = this.javaClass.getResource("/day5_1.txt")
            .readText()
            .split(',')
            .toVector()
        val state = Day5.State(program, "5")
        Day5.runIntProgram(state).output.joinToString(",") shouldBe "7616021"
    }


})