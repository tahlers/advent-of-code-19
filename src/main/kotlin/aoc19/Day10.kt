package aoc19

import io.vavr.Tuple2
import io.vavr.collection.HashMap
import io.vavr.collection.HashSet
import io.vavr.collection.Map
import io.vavr.collection.Set
import io.vavr.kotlin.tuple
import kotlin.math.atan2

object Day10 {

    data class Asteroid(val x: Int, val y: Int)

    fun parseField(field: String): Set<Asteroid> {
        return HashSet.ofAll(field.lines().mapIndexed { rowcount, line ->
            line.mapIndexed { columnCount, c ->
                if (c == '#') Asteroid(columnCount, rowcount) else null
            }.filterNotNull()
        }.flatten())
    }

    private fun canSee(asteroid1: Asteroid, asteroid2: Asteroid, field: Set<Asteroid>): Tuple2<Boolean, Double> {
        val diffX = asteroid2.x - asteroid1.x
        val diffY = asteroid2.y - asteroid1.y
        val angle = atan2(diffY.toDouble(), diffX.toDouble())
        val translate = (angle * 180 / Math.PI + 450) % 360
        val otherAsteroids = (field - asteroid1) - asteroid2
        val isSightBlocked = otherAsteroids.any { candidate ->
            val diffXCandidate = candidate.x - asteroid1.x
            val diffYCandidate = candidate.y - asteroid1.y
            val candidateAngle = atan2(diffYCandidate.toDouble(), diffXCandidate.toDouble())
            val smallerX = if (diffX > 0) diffXCandidate <= diffX else diffXCandidate >= diffX
            val smallerY = if (diffY > 0) diffYCandidate <= diffY else diffYCandidate >= diffY
            candidateAngle == angle && smallerX and smallerY
        }
        return tuple(!isSightBlocked, translate)
    }

    fun computeObservableMap(asteroids: Set<Asteroid>): Map<Asteroid, Int> {
        val entries = asteroids.map { asteroid ->
            val otherAsteroids = asteroids - asteroid
            val count = otherAsteroids.map { canSee(it, asteroid, asteroids) }.filter { it._1 }.size
            tuple(asteroid, count)
        }
        return HashMap.ofEntries(entries)
    }

    fun findLargestCountInObservableMap(map: Map<Asteroid, Int>): Tuple2<Asteroid, Int> {
        return map.maxBy { t -> t._2 }.orNull
    }

    fun findVaporizedAsteroid(station: Asteroid, asteroids: Set<Asteroid>, nth: Int): Asteroid {
        val others = asteroids - station
        val observable = others.map { it to canSee(station, it, asteroids) }
            .filter { it.second._1 }
            .sortedBy { it.second._2 }
            .map { it.first to it.second._2 + Math.PI }
        return if (nth <= observable.size) {
            observable[nth - 1].first
        } else {
            val newField = asteroids.removeAll(observable.map { it.first })
            findVaporizedAsteroid(station, newField, nth - observable.size)
        }
    }
}