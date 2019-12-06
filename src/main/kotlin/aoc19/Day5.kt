package aoc19

import io.vavr.collection.Vector
import jdk.nashorn.internal.runtime.regexp.joni.constants.OPCode

object Day5 {

    enum class Opcode(val code: String, val paramCount: Int) {
        ADD("01", 3),
        MUL("02", 3),
        INPUT("03", 1),
        OUTPUT("04", 1),
        JMPTRUE("05", 2),
        JMPFALSE("06", 2),
        LESS("07", 3),
        EQUALS("08", 3),
        HALT("99", 0);

        companion object {
            fun parse(code: String): Opcode {
                return when (code.takeLast(2)) {
                    "01", "1" -> ADD
                    "02", "2" -> MUL
                    "03", "3" -> INPUT
                    "04", "4" -> OUTPUT
                    "05", "5" -> JMPTRUE
                    "06", "6" -> JMPFALSE
                    "07", "7" -> LESS
                    "08", "8" -> EQUALS
                    "99" -> HALT
                    else -> throw IllegalArgumentException("Unknown Opcode $code")
                }
            }
        }
    }

    enum class ParamMode {
        POSITION, IMMEDIATE
    }

    data class Param(val value: Int, val mode: ParamMode)

    data class Instruction(val opcode: Opcode, val params: List<Param> = listOf())

    fun readInstruction(program: Vector<String>, offset: Int = 0): Instruction {
        val code = program[offset]
        val opcode = Opcode.parse(code)
        val paramModes = code.dropLast(2).padStart(opcode.paramCount, '0').reversed()
        val params = paramModes.mapIndexed { i, char ->
            val mode = if (char == '0') ParamMode.POSITION else ParamMode.IMMEDIATE
            Param(program[offset + i + 1].toInt(), mode)
        }
        return Instruction(opcode, params)
    }

    data class State(
        val program: Vector<String>,
        val input: String = "",
        val offset: Int = 0,
        val output: Vector<String> = Vector.empty()
    )

    fun runIntProgram(state: State): State {
        val program = state.program
        val offset = state.offset
        val input = state.input
        val instruction = readInstruction(program, offset)
        val nextOffset = offset + instruction.opcode.paramCount + 1
        return when (instruction.opcode) {
            Opcode.ADD -> {
                val op1 = evalParam(program, instruction.params[0])
                val op2 = evalParam(program, instruction.params[1])
                val storePosition = instruction.params[2].value
                val newProgram = program.update(storePosition, (op1 + op2).toString())
                runIntProgram(State(newProgram, input, nextOffset, state.output))
            }
            Opcode.MUL -> {
                val op1 = evalParam(program, instruction.params[0])
                val op2 = evalParam(program, instruction.params[1])
                val storePosition = instruction.params[2].value
                val newProgram = program.update(storePosition, (op1 * op2).toString())
                runIntProgram(State(newProgram, input, nextOffset, state.output))
            }
            Opcode.INPUT -> {
                val storePosition = instruction.params[0].value
                val newProgram = program.update(storePosition, input)
                runIntProgram(State(newProgram, input, nextOffset, state.output))
            }
            Opcode.OUTPUT -> {
                val output = evalParam(program, instruction.params[0]).toString()
                println("Output: $output")
                runIntProgram(State(program, input, nextOffset, state.output.append(output)))
            }
            Opcode.JMPTRUE -> {
                val op1 = evalParam(program, instruction.params[0])
                val op2 = evalParam(program, instruction.params[1])
                val newOffset = if (op1 != 0) op2 else nextOffset
                runIntProgram(state.copy(offset = newOffset))
            }
            Opcode.JMPFALSE -> {
                val op1 = evalParam(program, instruction.params[0])
                val op2 = evalParam(program, instruction.params[1])
                val newOffset = if (op1 == 0) op2 else nextOffset
                runIntProgram(state.copy(offset = newOffset))
            }
            Opcode.LESS -> {
                val op1 = evalParam(program, instruction.params[0])
                val op2 = evalParam(program, instruction.params[1])
                val storePosition = instruction.params[2].value
                val newProgram = program.update(storePosition, if (op1 < op2) "1" else "0")
                runIntProgram(State(newProgram, input, nextOffset, state.output))
            }
            Opcode.EQUALS -> {
                val op1 = evalParam(program, instruction.params[0])
                val op2 = evalParam(program, instruction.params[1])
                val storePosition = instruction.params[2].value
                val newProgram = program.update(storePosition, if (op1 == op2) "1" else "0")
                runIntProgram(State(newProgram, input, nextOffset, state.output))
            }
            Opcode.HALT -> {
                println("Program halted!")
                state
            }
        }

    }

    private fun evalParam(program: Vector<String>, param: Param): Int {
        return if (param.mode == ParamMode.POSITION) program[param.value].toInt() else param.value
    }


}