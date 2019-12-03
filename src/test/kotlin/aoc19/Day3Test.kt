package aoc19

import io.kotlintest.shouldBe
import io.kotlintest.specs.StringSpec

import aoc19.Day3.parseSteps
import aoc19.Day3.Step
import aoc19.Day3.Pos
import aoc19.Day3.Direction.*
import io.kotlintest.data.forall
import io.kotlintest.tables.row

class Day3Test : StringSpec({

    "parse wire string into a step list" {
        parseSteps("R75") shouldBe listOf(Step(RIGHT, 75))
        parseSteps("R75,L1,U3,D1") shouldBe listOf(
            Step(RIGHT, 75),
            Step(LEFT, 1),
            Step(UP, 3),
            Step(DOWN, 1)
        )
    }

    "convert steps to position lists" {
        Step(UP, 1).positions(Pos(0,0)) shouldBe listOf( Pos(0, 1))
        Step(DOWN, 2).positions(Pos(0,0)) shouldBe listOf(Pos(0, -1), Pos(0, -2))
        Step(RIGHT, 2).positions(Pos(0,0)) shouldBe listOf(Pos(1, 0), Pos(2, 0))
        Step(LEFT, 1).positions(Pos(0,0)) shouldBe listOf( Pos(-1, 0))

    }

    "lowest distance to origin for wires" {
        forall(
            row("R75,D30,R83,U83,L12,D49,R71,U7,L72", "U62,R66,U55,R34,D71,R55,D58,R83", 159),
            row("R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51", "U98,R91,D20,R16,D67,R40,U7,R15,U6,R7", 135)
        ) {wire1, wire2, expected ->
            Day3.distance(wire1, wire2) shouldBe expected
        }
    }

    "calculate solution one" {
        val lines = this.javaClass.getResource("/day3_1.txt").readText().lines()
        Day3.distance(lines[0], lines[1]) shouldBe 4981
    }

    "lowest wire length for wires" {
        forall(
            row("R75,D30,R83,U83,L12,D49,R71,U7,L72", "U62,R66,U55,R34,D71,R55,D58,R83", 610),
            row("R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51", "U98,R91,D20,R16,D67,R40,U7,R15,U6,R7", 410)
        ) {wire1, wire2, expected ->
            Day3.wireLength(wire1, wire2) shouldBe expected
        }
    }

    "calculate solution two" {
        val lines = this.javaClass.getResource("/day3_1.txt").readText().lines()
        Day3.wireLength(lines[0], lines[1]) shouldBe 164012
    }


})