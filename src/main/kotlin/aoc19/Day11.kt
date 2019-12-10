package aoc19

import io.vavr.Tuple2
import io.vavr.collection.Map
import io.vavr.collection.Vector
import io.vavr.collection.HashMap
import io.vavr.kotlin.component1
import io.vavr.kotlin.component2
import io.vavr.kotlin.toVavrStream
import io.vavr.kotlin.tuple

object Day11 {

    enum class Opcode(val paramCount: Int) {
        ADD(3),
        MUL(3),
        INPUT(1),
        OUTPUT(1),
        JMPTRUE(2),
        JMPFALSE(2),
        LESS(3),
        EQUALS(3),
        RELATIVE(1),
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
                    "09", "9" -> RELATIVE
                    "99" -> HALT
                    else -> throw IllegalArgumentException("Unknown Opcode $code")
                }
            }
        }
    }

    enum class ParamMode {
        POSITION, IMMEDIATE, RELATIVE
    }

    data class Param(val value: Long, val mode: ParamMode)

    data class Instruction(val opcode: Opcode, val params: List<Param> = listOf())

    private fun readInstruction(program: Map<Long, String>, offset: Long = 0): Instruction {
        val code = program[offset].getOrElse("0")
        val opcode = Opcode.parse(code)
        val paramModes = code.dropLast(2).padStart(opcode.paramCount, '0').reversed()
        val params = paramModes.mapIndexed { i, char ->
            val mode = when (char) {
                '0' -> ParamMode.POSITION
                '1' -> ParamMode.IMMEDIATE
                '2' -> ParamMode.RELATIVE
                else -> throw IllegalArgumentException("Unknown parameter mode $char")
            }
            Param(program[offset + i + 1].getOrElse("0").toLong(), mode)
        }
        return Instruction(opcode, params)
    }

    data class State(
        val program: Map<Long, String>,
        val input: Vector<String> = Vector.empty(),
        val offset: Long = 0,
        val relativeBase: Long = 0,
        val output: Vector<String> = Vector.empty()
    )

    fun readProgram(program: String): Map<Long, String> {
        return HashMap.ofEntries(program.split(",").toVavrStream().mapIndexed { index, s -> tuple(index.toLong(), s) })
    }

    private fun evalParam(program: Map<Long, String>, param: Param, relativeBase: Long): Long {
        return when (param.mode) {
            ParamMode.POSITION -> program[param.value].getOrElse("0").toLong()
            ParamMode.IMMEDIATE -> param.value
            ParamMode.RELATIVE -> program[param.value + relativeBase].getOrElse("0").toLong()
        }
    }

    private fun evalStoreParam(param: Param, relativeBase: Long): Long {
        return when (param.mode) {
            ParamMode.POSITION -> param.value
            ParamMode.RELATIVE -> param.value + relativeBase
            else -> throw java.lang.IllegalArgumentException("Immediate parameter not supported for store")
        }
    }

    private tailrec fun runIntProgram(state: State): State {
        val program = state.program
        val offset = state.offset
        val input = state.input
        val instruction = readInstruction(program, offset)
        val nextOffset = offset + instruction.opcode.paramCount + 1
        val relativeBase = state.relativeBase
        return when (instruction.opcode) {
            Opcode.ADD -> {
                val op1 = evalParam(program, instruction.params[0], relativeBase)
                val op2 = evalParam(program, instruction.params[1], relativeBase)
                val storePosition = evalStoreParam(instruction.params[2], relativeBase)
                val newProgram = program.put(storePosition, (op1 + op2).toString())
                runIntProgram(state.copy(program = newProgram, offset = nextOffset))
            }
            Opcode.MUL -> {
                val op1 = evalParam(program, instruction.params[0], relativeBase)
                val op2 = evalParam(program, instruction.params[1], relativeBase)
                val storePosition = evalStoreParam(instruction.params[2], relativeBase)
                val newProgram = program.put(storePosition, (op1 * op2).toString())
                runIntProgram(state.copy(program = newProgram, offset = nextOffset))
            }
            Opcode.INPUT -> {
                if (input.isEmpty) {
                    state
                } else {
                    val storePosition = evalStoreParam(instruction.params[0], relativeBase)
                    val newProgram = program.put(storePosition, input.head())
                    runIntProgram(state.copy(program = newProgram, input = input.tail(), offset = nextOffset))
                }
            }
            Opcode.OUTPUT -> {
                val output = evalParam(program, instruction.params[0], relativeBase).toString()
                runIntProgram(state.copy(offset = nextOffset, output = state.output.append(output)))
            }
            Opcode.JMPTRUE -> {
                val op1 = evalParam(program, instruction.params[0], relativeBase)
                val op2 = evalParam(program, instruction.params[1], relativeBase)
                val newOffset = if (op1 != 0L) op2 else nextOffset
                runIntProgram(state.copy(offset = newOffset))
            }
            Opcode.JMPFALSE -> {
                val op1 = evalParam(program, instruction.params[0], relativeBase)
                val op2 = evalParam(program, instruction.params[1], relativeBase)
                val newOffset = if (op1 == 0L) op2 else nextOffset
                runIntProgram(state.copy(offset = newOffset))
            }
            Opcode.LESS -> {
                val op1 = evalParam(program, instruction.params[0], relativeBase)
                val op2 = evalParam(program, instruction.params[1], relativeBase)
                val storePosition = evalStoreParam(instruction.params[2], relativeBase)
                val newProgram = program.put(storePosition, if (op1 < op2) "1" else "0")
                runIntProgram(state.copy(program = newProgram, offset = nextOffset))
            }
            Opcode.EQUALS -> {
                val op1 = evalParam(program, instruction.params[0], relativeBase)
                val op2 = evalParam(program, instruction.params[1], relativeBase)
                val storePosition = evalStoreParam(instruction.params[2], relativeBase)
                val newProgram = program.put(storePosition, if (op1 == op2) "1" else "0")
                runIntProgram(state.copy(program = newProgram, offset = nextOffset))
            }
            Opcode.RELATIVE -> {
                val op1 = evalParam(program, instruction.params[0], relativeBase)
                val newRelativeBase = relativeBase + op1
                runIntProgram(state.copy(relativeBase = newRelativeBase, offset = nextOffset))
            }
            Opcode.HALT -> {
                state
            }
        }
    }

    enum class Color { BLACK, WHITE }
    enum class Direction {
        UP, DOWN, LEFT, RIGHT;

        companion object {
            fun move(input: String, currentDir: Direction, pos: Pos): Tuple2<Direction, Pos> {
                val newDirection = when (currentDir) {
                    UP -> if (input == "0") LEFT else RIGHT
                    RIGHT -> if (input == "0") UP else DOWN
                    DOWN -> if (input == "0") RIGHT else LEFT
                    LEFT -> if (input == "0") DOWN else UP
                }
                return when (newDirection) {
                    UP -> tuple(newDirection, pos.copy(y = pos.y + 1))
                    RIGHT -> tuple(newDirection, pos.copy(x = pos.x + 1))
                    DOWN -> tuple(newDirection, pos.copy(y = pos.y - 1))
                    LEFT -> tuple(newDirection, pos.copy(x = pos.x - 1))

                }
            }
        }
    }

    data class Pos(val x: Int, val y: Int)

    tailrec fun paintJob(
        state: State,
        visited: Map<Pos, Color>,
        currentPos: Pos,
        currentColor: Color,
        currentDir: Direction
    ): Map<Pos, Color> {
        val currentOpcode = Opcode.parse(state.program[state.offset].getOrElse("0"))
        return if (currentOpcode != Opcode.HALT) {

            val newInput = Vector.of(if (currentColor == Color.BLACK) "0" else "1")
            val newState = runIntProgram(state.copy(input = newInput, output = Vector.empty()))
            val paintColor = if (newState.output[0] == "0") Color.BLACK else Color.WHITE
            val newVisited = visited.put(currentPos, paintColor)
            val newDirInput = newState.output[1]
            val (newDir, newPos) = Direction.move(newDirInput, currentDir, currentPos)
            val newColor = visited.getOrElse(newPos, Color.BLACK)
            paintJob(newState, newVisited, newPos, newColor, newDir)
        } else {
            visited
        }
    }

    fun printGrid(grid: Map<Pos, Color>) {
        val minX = grid.keySet().minBy { it -> it.x }.map { it.x }.get()
        val minY = grid.keySet().minBy { it -> it.y }.map { it.y }.get()
        val maxX = grid.keySet().maxBy { it -> it.x }.map { it.x }.get()
        val maxY = grid.keySet().maxBy { it -> it.y }.map { it.y }.get()
        (maxY downTo minY).forEach { row ->
            (minX..maxX).forEach { pixel ->
                val color = grid[Pos(pixel, row)].getOrElse(Color.BLACK)
                if (color == Color.WHITE) print("█") else print(" ")
            }
            print("\n")
        }
    }
}