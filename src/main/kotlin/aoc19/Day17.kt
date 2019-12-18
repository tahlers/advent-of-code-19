package aoc19

import io.vavr.collection.HashMap
import io.vavr.collection.Map
import io.vavr.collection.Vector
import io.vavr.control.Option
import io.vavr.kotlin.component1
import io.vavr.kotlin.component2
import io.vavr.kotlin.none
import io.vavr.kotlin.some
import io.vavr.kotlin.toVavrStream
import io.vavr.kotlin.tuple
import kotlin.streams.toList

object Day17 {

    operator fun <T> Vector<T>.component1(): T = this[0]
    operator fun <T> Vector<T>.component2(): T = this[1]
    operator fun <T> Vector<T>.component3(): T = this[2]

    fun <T> Vector<T>.split(sub: Vector<T>): Vector<Vector<T>> {
        val sliceOption = this.indexOfSliceOption(sub)
        return if (sliceOption.isEmpty) Vector.of(this) else {
            val index = sliceOption.get()
            val remainder = this.subSequence(index + sub.length())
            Vector.of(this.subSequence(0, index), sub).appendAll(remainder.split(sub))
        }
    }

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

    enum class Turn(val symbol: Char) { LEFT('L'), RIGHT('R'); }

    enum class Dir(private val moveFun: (Pos) -> Pos) {
        NORTH({ Pos(it.x, it.y - 1) }),
        SOUTH({ Pos(it.x, it.y + 1) }),
        EAST({ Pos(it.x - 1, it.y) }),
        WEST({ Pos(it.x + 1, it.y) });

        fun moveFrom(pos: Pos): Pos = moveFun(pos)
        val left: Dir
            get() = when (this) {
                NORTH -> EAST
                SOUTH -> WEST
                EAST -> SOUTH
                WEST -> NORTH
            }
        val right: Dir
            get() = when (this) {
                NORTH -> WEST
                SOUTH -> EAST
                EAST -> NORTH
                WEST -> SOUTH
            }
    }

    data class MoveStep(val turn: Turn, val steps: Int) {
        fun inc() = this.copy(steps = steps + 1)
        override fun toString(): String = turn.symbol + steps.toString()
        fun toAscii(): List<String> = listOf(turn.symbol.toInt().toString(), "44") + steps.toString().map { it.toInt().toString() }
    }

    data class Pos(val x: Int, val y: Int)

    data class Robot(val pos: Pos, val dir: Dir, val path: Vector<MoveStep>)

    tailrec fun calcRobotPath(robot: Robot, image: Map<Pos, Char>): Robot {
        val nextStepPos = robot.dir.moveFrom(robot.pos)
        return if (isPosAvailable(nextStepPos, image)) {
            val newMoveStep = robot.path.last().inc()
            val newPath = robot.path.update(robot.path.length() - 1, newMoveStep)
            calcRobotPath(robot.copy(pos = nextStepPos, path = newPath), image)
        } else if (isPosAvailable(robot.dir.left.moveFrom(robot.pos), image)) {
            val newDir = robot.dir.left
            val newPath = robot.path.append(MoveStep(Turn.LEFT, 0))
            val newRobot = Robot(robot.pos, newDir, newPath)
            calcRobotPath(newRobot, image)
        } else if (isPosAvailable(robot.dir.right.moveFrom(robot.pos), image)) {
            val newDir = robot.dir.right
            val newPath = robot.path.append(MoveStep(Turn.RIGHT, 0))
            val newRobot = Robot(robot.pos, newDir, newPath)
            calcRobotPath(newRobot, image)
        } else {
            robot
        }
    }

    private fun isPosAvailable(pos: Pos, image: Map<Pos, Char>): Boolean =
        image[pos].getOrElse('.') == '#'

    fun reportDust(state: State): String {
        val image = getImage(state)
        val startPos = image
            .find { (_, ch) -> ch == '^' }
            .map { it._1 }
            .getOrElse(Pos(0, 0))
        val initialRobot = Robot(startPos, Dir.NORTH, Vector.empty())
        val finalRobot = calcRobotPath(initialRobot, image)
        val pathString = finalRobot.path.mkString()
        val subs = findSubFunctions(Vector.of(finalRobot.path), this::hasAllowedLength, 3).get()
        val (subA, subB, subC) = subs

        val replacedSubs = pathString
            .replace(subA.mkString(), "A")
            .replace(subB.mkString(), "B")
            .replace(subC.mkString(), "C")

        val mainInput = replacedSubs.chars().toList()
            .map { it.toChar() }.joinToString(",").map { it.toInt().toString() } + "10"
        val subAInput = subA.map { it.toAscii() }.intersperse(listOf("44")).flatten() + "10"
        val subBInput = subB.map { it.toAscii() }.intersperse(listOf("44")).flatten() + "10"
        val subCInput = subC.map { it.toAscii() }.intersperse(listOf("44")).flatten() + "10"
        val video = listOf('n'.toInt().toString(), "10")

        println(pathString)
        println("A: ${subA.mkString()}, B: ${subB.mkString()}, C: ${subC.mkString()}")
        println("Program: $replacedSubs")
        println("SubA: $subAInput")
        println("SubB: $subBInput")
        println("SubC: $subCInput")

        val newInput = mainInput + subAInput + subBInput + subCInput + video
        val newState = state.copy(program = state.program.put(0, "2"), input = newInput.toVector())
        val finalState = runIntProgram(newState)
        getImage(finalState)
        return finalState.output.last()
    }

    /* adapted from this solution https://gist.github.com/rasmusfaber/82294f98979306fc2224ded75c3c9b2a */
    private fun <T> findSubFunctions(
        input: Vector<Vector<T>>, isValid: (Vector<T>) -> Boolean, maxSubFunctions: Int
    ): Option<Vector<Vector<T>>> {
        if (input.isEmpty) return some(Vector.empty())
        if (maxSubFunctions == 0) return none()
        for (i in 1..input[0].length()) {
            val candidate = input[0].subSequence(0, i)
            if (!isValid(candidate)) {
                break
            }
            val fragments = input.flatMap { it.split(candidate) }.reject { it.isEmpty }
            val otherFragments = fragments.removeAll(candidate)
            val res = findSubFunctions(otherFragments, isValid, maxSubFunctions - 1)
            res.forEach { return some(it.prepend(candidate)) }
        }
        return none()
    }

    private fun hasAllowedLength(path: Vector<MoveStep>): Boolean = path.mkString(",").length <= 20

    fun getImage(state: State): Map<Pos, Char> {
        val runState = runIntProgram(state)
        val outputString = runState.output.map { it.toInt().toChar() }.mkString()
        println("Image: \n $outputString")
        val lines = outputString.lines()
        val entryList = lines.mapIndexed { row, line ->
            line.mapIndexed { column, char ->
                tuple(Pos(column, row), char)
            }
        }.flatten()
        return HashMap.ofEntries(entryList)
    }

    fun getAlignmentParam(state: State): Int {
        val image = getImage(state)
        val intersections = image.filter { (x, y), char ->
            val isScaffold = char == '#'
            val hasNorthScaffold = image[Pos(x, y - 1)].getOrElse(' ') == '#'
            val hasSouthScaffold = image[Pos(x, y + 1)].getOrElse(' ') == '#'
            val hasEastScaffold = image[Pos(x - 1, y)].getOrElse(' ') == '#'
            val hasWestScaffold = image[Pos(x + 1, y)].getOrElse(' ') == '#'
            isScaffold && hasNorthScaffold && hasSouthScaffold && hasEastScaffold && hasWestScaffold
        }
        val alignmentParams = intersections.keySet().map { it.x * it.y }
        return alignmentParams.sum().toInt()
    }
}