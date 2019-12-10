package aoc19

import io.vavr.collection.HashSet
import io.vavr.collection.Stream
import io.vavr.collection.Vector
import java.util.Arrays
import kotlin.math.abs

object Day12 {

    data class Vec(val x: Int, val y: Int, val z: Int) {
        operator fun plus(other: Vec): Vec = Vec(x + other.x, y + other.y, z + other.z)
        fun abs(): Int = abs(x) + abs(y) + abs(z)
    }

    data class Moon(val pos: Vec, val velocity: Vec = Vec(0, 0, 0)) {
        fun energy(): Int = pos.abs() * velocity.abs()
        fun updateWithGravity(others: Vector<Moon>): Moon {
            val deltaVelocities = others.remove(this).map { moon ->
                Vec(
                    deltaV(pos.x, moon.pos.x),
                    deltaV(pos.y, moon.pos.y),
                    deltaV(pos.z, moon.pos.z)
                )
            }
            val newVelocity = deltaVelocities.fold(velocity, Vec::plus)
            val newPos = pos + newVelocity
            return Moon(newPos, newVelocity)
        }
    }

    private fun deltaV(a: Int, b: Int): Int {
        return when {
            a < b -> 1
            a > b -> -1
            else -> 0
        }
    }

    fun updateMoons(moons: Vector<Moon>): Stream<Vector<Moon>> {
        val stepStream = Stream.iterate(moons) { it.map { moon -> moon.updateWithGravity(it) } }
        return stepStream
    }

    fun Vector<Moon>.energy() = this.map { it.energy() }.sum().toInt()

    enum class Axis { X, Y, Z }

    private fun repeatAxisAfter(moons: Vector<Moon>, axis: Axis): Long {
        Stream.from(0L).fold(moons to HashSet.empty<Vector<Pair<Int, Int>>>()) { (currentMoons, set), count ->
            val updated = currentMoons.map { it.updateWithGravity(currentMoons) }
            val updatedAxis = when (axis) {
                Axis.X -> updated.map { it.pos.x to it.velocity.x }
                Axis.Y -> updated.map { it.pos.y to it.velocity.y }
                Axis.Z -> updated.map { it.pos.z to it.velocity.z }
            }
            if (set.contains(updatedAxis)) return count
            updated to set.add(updatedAxis)
        }
        return -1
    }

    fun repeatAfter(moons: Vector<Moon>): Long {

        val repeatXCount = repeatAxisAfter(moons, Axis.X)
        val repeatYCount = repeatAxisAfter(moons, Axis.Y)
        val repeatZCount = repeatAxisAfter(moons, Axis.Z)

        return lcm(repeatXCount, repeatYCount, repeatZCount)
    }

    private fun gcd(x: Long, y: Long): Long {
        return if (y == 0L) x else gcd(y, x % y)
    }

    private fun lcm(vararg numbers: Long): Long {
        return Arrays.stream(numbers).reduce(1L) { x, y -> x * (y / gcd(x, y)) }
    }
}