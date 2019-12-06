package aoc19

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec
import io.vavr.collection.HashMap

class Day6Test : StringSpec({

    "should parse orbit maps" {
        val orbits = """
            COM)B
            B)C
            C)D
        """.trimIndent()
        val orbitMap = Day6.parseOrbits(orbits)
        orbitMap shouldBe HashMap.of("B", "COM", "C", "B", "D", "C")
    }

    "should calculate number of direct and indirect orbits" {
        val orbits = """
            COM)B
            B)C
            C)D
            D)E
            E)F
            B)G
            G)H
            D)I
            E)J
            J)K
            K)L
        """.trimIndent()
        val orbitMap = Day6.parseOrbits(orbits)
        Day6.countOrbits(orbitMap) shouldBe 42
    }

    "calculation solution one " {
        val orbitString = this.javaClass.getResource("/day6_1.txt")
            .readText()
        val orbitMap = Day6.parseOrbits(orbitString)
        Day6.countOrbits(orbitMap) shouldBe 273985
    }

    "should calculate number of orbit transitions" {
        val orbits = """
            COM)B
            B)C
            C)D
            D)E
            E)F
            B)G
            G)H
            D)I
            E)J
            J)K
            K)L
            K)YOU
            I)SAN
        """.trimIndent()
        val orbitMap = Day6.parseOrbits(orbits)
        Day6.numberOfTransitions("YOU", "SAN", orbitMap) shouldBe 4
    }

    "calculation solution two " {
        val orbitString = this.javaClass.getResource("/day6_1.txt")
            .readText()
        val orbitMap = Day6.parseOrbits(orbitString)
        Day6.numberOfTransitions("YOU", "SAN", orbitMap) shouldBe 460
    }
})