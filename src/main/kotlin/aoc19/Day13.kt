package aoc19

import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import io.vavr.collection.HashMap
import io.vavr.collection.HashSet
import io.vavr.collection.Map
import io.vavr.collection.Set
import io.vavr.collection.Vector
import io.vavr.kotlin.toVavrStream
import io.vavr.kotlin.tuple

object Day13 {

    operator fun <T> Vector<T>.component1(): T = this[0]
    operator fun <T> Vector<T>.component2(): T = this[1]
    operator fun <T> Vector<T>.component3(): T = this[2]

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
    ) {
        val currentOpcode = Day11.Opcode.parse(program[offset].getOrElse("0"))
    }

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

    enum class TileType(val symbol: Char) {
        EMPTY(' '),
        WALL('█'),
        BLOCK('#'),
        HPADDLE('='),
        BALL('*');
        companion object {
            fun parse(input: Int) = values()[input]
        }
    }

    data class Pos(val x: Int, val y: Int)

    data class Tile(val tileId: Pos, val type: TileType)

    fun computeWorld(program: Map<Long, String>): Set<Tile> {
        val initialState = State(program)
        val finalState = runIntProgram(initialState)
        val outputGrouped = finalState.output.grouped(3)
        val tiles = outputGrouped.map { (x, y, tileType) ->
            Tile(Pos(x.toInt(), y.toInt()), TileType.parse(tileType.toInt()))
        }
        return HashSet.ofAll(tiles)
    }

    tailrec fun playGame(state: State, world: Map<Pos, TileType>, score: Int, screen: Screen? = null): Int {
        if (screen != null) {
            world.forEach { pos, tile ->
                screen.setCharacter(pos.x, pos.y, TextCharacter(tile.symbol))
            }
            screen.refresh()
            Thread.sleep(25L)
        }
        return if (state.currentOpcode == Day11.Opcode.HALT) {
            score
        } else {
            val newInput = joyInput(world)
            val newPlayState = state.copy(input = Vector.of(newInput), output = Vector.empty())
            val updatedState = runIntProgram(newPlayState)
            val (newWorld, newScore) = updateWorldAndScore(world, score, updatedState.output)
            playGame(updatedState, newWorld, newScore, screen)
        }
    }

    private fun updateWorldAndScore(screen: Map<Pos, TileType>,
        score: Int,
        updates: Vector<String>
    ): Pair<Map<Pos, TileType>, Int> {
        return updates.grouped(3).fold(screen to score) { (screen, score), (x, y, tileType) ->
            val isScoreUpdate = x == "-1"
            val newScore = if (isScoreUpdate) tileType.toInt() else score
            val newScreen = if (isScoreUpdate) screen else {
                screen.put(Pos(x.toInt(), y.toInt()), TileType.parse(tileType.toInt()))
            }
            newScreen to newScore
        }
    }

    private fun joyInput(world: Map<Pos, TileType>): String {
        val ballX = world.find { it._2 == TileType.BALL }
            .map { it._1.x}
            .getOrElse(0)
        val paddleX = world.find {it._2 == TileType.HPADDLE}
            .map { it._1.x}
            .getOrElse(0)
        return deltaV(paddleX, ballX).toString()
    }

    private fun deltaV(a: Int, b: Int): Int = when {a < b -> 1; a > b -> -1 ; else -> 0 }


    fun finalScore(program: Map<Long, String>): Int {
        return playGame(State(program), HashMap.empty(), 0)
    }

    fun playWithTerminal(program: Map<Long, String>) {
        val defaultTerminalFactory = DefaultTerminalFactory()
        defaultTerminalFactory.createScreen().use { screen ->
            screen.startScreen()
            screen.cursorPosition = null
            playGame(State(program), HashMap.empty(), 0, screen)
        }
    }

}