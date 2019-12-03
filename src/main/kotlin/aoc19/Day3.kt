package aoc19

import kotlin.math.abs

object Day3 {

    enum class Direction { UP, DOWN, LEFT, RIGHT }

    data class Step(val direction: Direction, val distance: Int) {
        fun positions(startPos: Pos): List<Pos> {
            return when (direction) {
                Direction.UP -> ((startPos.y + 1)..(startPos.y + distance)).map { Pos(startPos.x, it) }
                Direction.DOWN -> ((startPos.y - 1) downTo (startPos.y - distance)).map { Pos(startPos.x, it) }
                Direction.RIGHT -> ((startPos.x + 1)..(startPos.x + distance)).map { Pos(it, startPos.y) }
                Direction.LEFT -> ((startPos.x - 1) downTo (startPos.x - distance)).map { Pos(it, startPos.y) }
            }
        }
    }

    data class Pos(val x: Int, val y: Int) {
        fun distance(other: Pos): Int {
            return abs(x - other.x) + abs(y - other.y)
        }
    }

    fun parseSteps(stepsString: String): List<Step> {
        return stepsString.split(',').map { s ->
            val dir = s[0]
            val distance = s.substring(1).toInt()
            when (dir) {
                'R' -> Step(Direction.RIGHT, distance)
                'L' -> Step(Direction.LEFT, distance)
                'U' -> Step(Direction.UP, distance)
                'D' -> Step(Direction.DOWN, distance)
                else -> throw IllegalArgumentException("Unparsable step $s")
            }
        }
    }

    fun distance(wire1: String, wire2: String): Int {
        val wire1Steps = parseSteps(wire1)
        val wire2Steps = parseSteps(wire2)
        val wire1Positions = wire1Steps.fold(listOf(Pos(0, 0))) { init, step ->
            init + step.positions(init.last())
        }.drop(1)
        val wire2Positions = wire2Steps.fold(listOf(Pos(0, 0))) { init, step ->
            init + step.positions(init.last())
        }.drop(1)
        val intersections = wire1Positions.intersect(wire2Positions)
        val distances = intersections.map { it.distance(Pos(0, 0)) }
        return distances.min() ?: 0
    }

    fun wireLength(wire1: String, wire2: String): Int {
        val wire1Steps = parseSteps(wire1)
        val wire2Steps = parseSteps(wire2)
        val wire1Positions = wire1Steps.fold(listOf(Pos(0, 0))) { init, step ->
            init + step.positions(init.last())
        }.drop(1)
        val wire2Positions = wire2Steps.fold(listOf(Pos(0, 0))) { init, step ->
            init + step.positions(init.last())
        }.drop(1)
        val intersections = wire1Positions.intersect(wire2Positions)
        val wire1PositionsIndexed = wire1Positions.withIndex().filter { it.value in intersections }
        val wire2PositionsIndexed = wire2Positions.withIndex().filter { it.value in intersections }
        val wire1Map = wire1PositionsIndexed.groupBy({ it.value }) { it.index + 1 }
        val wire2Map = wire2PositionsIndexed.groupBy({ it.value }) { it.index + 1 }
        return intersections.map {
            (wire1Map[it]?.min() ?: 0) + (wire2Map[it]?.min() ?: 0)
        }.min() ?: 0
    }
}