package aoc19

import io.vavr.collection.Vector

object Day2 {

    fun runIntCode(program: Vector<Int>, offset: Int = 0): Vector<Int> {
        val opcode = program[offset]
        return if (opcode != 99 ) {
            val operand1 = program[program[offset+1]]
            val operand2 = program[program[offset+2]]
            val storePosition = program[offset+3]
            val newProgram = when (opcode) {
                1 -> program.update(storePosition, operand1 + operand2)
                2 -> program.update(storePosition, operand1 * operand2)
                else -> throw IllegalStateException("Unknown Opcode $opcode")
            }
            runIntCode(newProgram, offset + 4)
        } else program
    }

    fun discoverResult(program: Vector<Int>, endState: Int): Int {
        for (op1 in 0..99) {
            for (op2 in 0..99){
                val updatedProgram = program.update(1, op1).update(2, op2)
                val run = runIntCode(updatedProgram)
                if (run[0] == endState) {
                    return run[1] * 100 + run[2]
                }
            }
        }
        return 0
    }


}