package aoc19

import com.googlecode.lanterna.TextCharacter
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import io.vavr.Tuple3
import io.vavr.collection.HashMap
import io.vavr.collection.HashSet
import io.vavr.collection.Map
import io.vavr.collection.Set
import io.vavr.collection.Vector
import io.vavr.control.Option
import io.vavr.kotlin.component1
import io.vavr.kotlin.component2
import io.vavr.kotlin.component3
import io.vavr.kotlin.toVavrStream
import io.vavr.kotlin.tuple
import kotlin.math.abs

object Day15 {

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

    enum class MoveTo(val input: String) { NORTH("1"), SOUTH("2"), WEST("3"), EAST("4") }

    enum class Tile(val symbol: Char) {
        WALL('█'),
        EMPTY('.'),
        SYSTEM('#');

        companion object {
            fun parse(input: Int) = values()[input]
        }
    }

    data class Pos(val x: Int, val y: Int) {
        fun move(moveTo: MoveTo): Pos {
            return when (moveTo) {
                MoveTo.NORTH -> Pos(x, y + 1)
                MoveTo.SOUTH -> Pos(x, y - 1)
                MoveTo.EAST -> Pos(x + 1, y)
                MoveTo.WEST -> Pos(x - 1, y)
            }
        }

        fun direction(other: Pos): MoveTo {
            return when {
                other.x - x < 0 -> MoveTo.WEST
                other.x - x > 0 -> MoveTo.EAST
                other.y - y < 0 -> MoveTo.SOUTH
                other.y - y > 0 -> MoveTo.NORTH
                else -> MoveTo.NORTH
            }
        }
    }

    private fun move(
        currentPos: Pos,
        direction: MoveTo,
        map: Map<Pos, Tile>,
        state: State
    ): Tuple3<Pos, Map<Pos, Tile>, State> {
        val newInput = direction.input
        val preparedState = state.copy(input = Vector.of(newInput), output = Vector.empty())
        val updatedState = runIntProgram(preparedState)
        val output = updatedState.output[0]
        val tile = Tile.parse(output.toInt())
        val newPos = currentPos.move(direction)
        val updatedMap = map.put(newPos, tile)
        return if (tile == Tile.WALL) {
            tuple(currentPos, updatedMap, updatedState)
        } else {
            tuple(newPos, updatedMap, updatedState)
        }
    }

    private fun nextUnknownDir(pos: Pos, map: Map<Pos, Tile>): Option<MoveTo> {
        return Option.of(
            MoveTo.values().firstOrNull { dir ->
                val newPos = pos.move(dir)
                !map.containsKey(newPos)
            }
        )
    }

    private fun validNextDirs(pos: Pos, map: Map<Pos, Tile>, forbidden: Set<Pos>): Vector<MoveTo> {
        return MoveTo.values().filter { dir ->
            val newPos = pos.move(dir)
            val isNotWall = !(map[newPos].map { it == Tile.WALL }.getOrElse(true))
            val isForbidden = newPos !in forbidden
            isNotWall && isForbidden
        }.toVector()
    }

    private tailrec fun exploreMap(posList: Vector<Pos>, map: Map<Pos, Tile>, state: State): Map<Pos, Tile> {
        return if (posList.isEmpty) return map else {
            val pos = posList.last()
            val newDirOption = nextUnknownDir(pos, map)
            if (newDirOption.isDefined) {
                val (newPos, newMap, newState) = move(pos, newDirOption.get(), map, state)
                val newPosList = if (newPos == pos) posList else posList.append(newPos)
                exploreMap(newPosList, newMap, newState)
            } else { // move back
                val newPosList = posList.init()
                if (newPosList.isEmpty) return map
                val dir = pos.direction(newPosList.last())
                val (newPos, newMap, newState) = move(pos, dir, map, state)
                if (newPos != newPosList.last()) {
                    throw IllegalStateException("Backtracking did not work!")
                }
                exploreMap(posList.init(), newMap, newState)
            }
        }
    }

    tailrec fun bfsThroughMaze(
        currentPaths: Vector<Vector<Pos>>,
        map: Map<Pos, Tile>,
        finished: Vector<Vector<Pos>>,
        visited: Set<Pos>,
        finishFunction: (Vector<Pos>, Map<Pos, Tile>, Set<Pos>) -> Boolean
    ): Vector<Vector<Pos>> {
        val newVisited = visited.addAll(currentPaths.map { it.last() })
        val (newlyFinished, todo) = currentPaths.partition { finishFunction(it, map, visited) }
        return if (todo.isEmpty) finished.appendAll(newlyFinished) else {
            val newPaths = todo.flatMap { path ->
                val pos = path.last()
                val validNextDirs = validNextDirs(pos, map, newVisited)
                val nextPositions = validNextDirs.map { pos.move(it) }
                nextPositions.map { path.append(it) }
            }
            bfsThroughMaze(newPaths, map, finished.appendAll(newlyFinished), newVisited, finishFunction)
        }
    }

    fun countMoves(program: Map<Long, String>): Int {
        val initPosList = Vector.of(Pos(0, 0))
        val initMap = HashMap.of(Pos(0, 0), Tile.EMPTY)
        val initState = State(program)
        val field = exploreMap(initPosList, initMap, initState)
        val finishFunction: (Vector<Pos>, Map<Pos, Tile>, Set<Pos>) -> Boolean = { path, map, _ ->
            map[path.last()].getOrElse(Tile.EMPTY) == Tile.SYSTEM
        }
        val systemPaths = bfsThroughMaze(Vector.of(initPosList), field, Vector.empty(), HashSet.empty(), finishFunction)
        return systemPaths.map { it.size() }.min().getOrElse(0) - 1
    }

    fun calcOxygenFill(program: Map<Long, String>): Int {
        val initPosList = Vector.of(Pos(0, 0))
        val initMap = HashMap.of(Pos(0, 0), Tile.EMPTY)
        val initState = State(program)
        val field = exploreMap(initPosList, initMap, initState)
        val finishFunction: (Vector<Pos>, Map<Pos, Tile>, Set<Pos>) -> Boolean = { path, map, visited ->
            validNextDirs(path.last(), map, visited).isEmpty
        }
        //printMaze(field)
        val systemPos = field.first { it._2 == Tile.SYSTEM }._1
        val initialPaths = Vector.of(Vector.of(systemPos))
        val oxygenPaths = bfsThroughMaze(initialPaths, field, Vector.empty(), HashSet.empty(), finishFunction)
        return oxygenPaths.map { it.size()}.max().getOrElse(0) - 1
    }

    fun printMaze(maze: Map<Pos, Tile>) {
        val offsetX = abs(maze.keySet().map { it.x }.min().get())
        val offsetY = abs(maze.keySet().map { it.y }.min().get())
        val defaultTerminalFactory = DefaultTerminalFactory()
        defaultTerminalFactory.createSwingTerminal()
        defaultTerminalFactory.createScreen().use { screen ->
            screen.startScreen()
            screen.readInput()
            screen.doResizeIfNecessary()
            screen.cursorPosition = null
            maze.forEach { (pos, tile) ->
                screen.setCharacter(pos.x + offsetX, pos.y + offsetY,
                    TextCharacter(tile.symbol, TextColor.ANSI.WHITE, TextColor.ANSI.BLACK))
            }

            screen.refresh()
            screen.readInput()
        }
    }
}