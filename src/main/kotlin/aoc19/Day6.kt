package aoc19

import io.vavr.collection.HashMap
import io.vavr.collection.Map
import io.vavr.kotlin.tuple

object Day6 {

    fun parseOrbits(orbits: String): Map<String, String> {
        val lines = orbits.split("\n")
        val entries = lines.map {
            val (center, satellite) = it.split(')')
            tuple(satellite, center)
        }
        return HashMap.ofEntries(entries)
    }

    private fun generateOrbits(
        sat: String,
        orbits: Map<String, String>,
        orbitPaths: Map<String, List<String>>
    ): Map<String, List<String>> {
        return if (sat in orbitPaths.keySet()) {
            orbitPaths
        } else {
            val center = orbits.getOrElse(sat, "")
            if (orbits.containsKey(center)) {
                val updatedPaths = generateOrbits(center, orbits, orbitPaths)
                val centerPath = updatedPaths.getOrElse(center, emptyList())
                updatedPaths.put(sat, listOf(sat) + centerPath)
            } else {
                orbitPaths.put(sat, listOf(sat, center))
            }
        }
    }

    fun countOrbits(orbits: Map<String, String>): Int {
        val initMap: Map<String, List<String>> = HashMap.empty()
        val orbitMap = orbits.keysIterator().fold(initMap) { map, sat ->
            generateOrbits(sat, orbits, map)
        }

        return orbitMap.values().map { it.size - 1 }.toJavaList().sum()
    }

    fun numberOfTransitions(sat1: String, sat2: String, orbits: Map<String, String>): Int {
        val initMap: Map<String, List<String>> = HashMap.empty()
        val orbitMap = orbits.keysIterator().fold(initMap) { map, sat ->
            generateOrbits(sat, orbits, map)
        }
        val sat1Orbits = orbitMap.getOrElse(sat1, listOf(sat1))
        val sat2Orbits = orbitMap.getOrElse(sat2, listOf(sat2))
        val sat1Transitions = sat1Orbits.takeWhile { it !in sat2Orbits }.drop(1)
        val sat2Transitions = sat2Orbits.takeWhile { it !in sat1Orbits }.drop(1)

        return sat1Transitions.size + sat2Transitions.size
    }
}