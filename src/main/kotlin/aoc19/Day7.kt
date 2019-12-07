package aoc19

import io.vavr.collection.Stream
import io.vavr.collection.Vector

object Day7 {

    enum class Opcode(val paramCount: Int) {
        ADD(3),
        MUL(3),
        INPUT(1),
        OUTPUT(1),
        JMPTRUE(2),
        JMPFALSE(2),
        LESS(3),
        EQUALS(3),
        HALT(0);

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
        val input: Vector<String> = Vector.empty(),
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
                if (input.isEmpty) {
                    state
                } else {
                    val storePosition = instruction.params[0].value
                    val newProgram = program.update(storePosition, input.head())
                    runIntProgram(State(newProgram, input.tail(), nextOffset, state.output))
                }
            }
            Opcode.OUTPUT -> {
                val output = evalParam(program, instruction.params[0]).toString()
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
                state
            }
        }
    }

    private fun evalParam(program: Vector<String>, param: Param): Int {
        return if (param.mode == ParamMode.POSITION) program[param.value].toInt() else param.value
    }

    private fun calculateAmpOutput(program: Vector<String>, input: Vector<String>, phase: String): Vector<String> {
        val state = State(program, input.prepend(phase))
        return runIntProgram(state).output
    }

    fun calculateMaxAmpsOutput(program: Vector<String>): Int {
        val phaseSettings = Vector.of("0", "1", "2", "3", "4").permutations()
        val outputs = phaseSettings.map { p ->
            val out1 = calculateAmpOutput(program, Vector.of("0"), p[0])
            val out2 = calculateAmpOutput(program, out1, p[1])
            val out3 = calculateAmpOutput(program, out2, p[2])
            val out4 = calculateAmpOutput(program, out3, p[3])
            val out5 = calculateAmpOutput(program, out4, p[4])
            out5[0].toInt()
        }
        return outputs.max().get()
    }

    fun calculateMaxAmpsOutputFeedback(program: Vector<String>): Int {
        val phaseSettings = Vector.of("5", "6", "7", "8", "9").permutations()
        val outputs = phaseSettings.map { p ->

            val amp1 = runIntProgram(State(program, Vector.of(p[0], "0")))
            val amp2 = runIntProgram(State(program, amp1.output.prepend(p[1])))
            val amp3 = runIntProgram(State(program, amp2.output.prepend(p[2])))
            val amp4 = runIntProgram(State(program, amp3.output.prepend(p[3])))
            val amp5 = runIntProgram(State(program, amp4.output.prepend(p[4])))

            val ampStream = Stream.iterate(listOf(amp1, amp2, amp3, amp4, amp5)) { amps ->
                val newAmp1 = runIntProgram(amps[0].copy(input = Vector.of(amps[4].output.last())))
                val newAmp2 = runIntProgram(amps[1].copy(input = Vector.of(newAmp1.output.last())))
                val newAmp3 = runIntProgram(amps[2].copy(input = Vector.of(newAmp2.output.last())))
                val newAmp4 = runIntProgram(amps[3].copy(input = Vector.of(newAmp3.output.last())))
                val newAmp5 = runIntProgram(amps[4].copy(input = Vector.of(newAmp4.output.last())))
                listOf(newAmp1, newAmp2, newAmp3, newAmp4, newAmp5)
            }

            val outputAmp = ampStream.first { amps ->
                readInstruction(amps[4].program, amps[4].offset).opcode == Opcode.HALT
            }[4]

            outputAmp.output.last().toInt()
        }
        return outputs.max().get()
    }
}