package aoc19

import aoc19.Day12.Moon
import aoc19.Day12.Vec
import aoc19.Day12.energy
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.vavr.collection.Vector

class Day12Test : FreeSpec({

    "sample data one" - {
        val moons = Vector.of(
            Moon(Vec(-1, 0, 2)),
            Moon(Vec(2, -10, -7)),
            Moon(Vec(4, -8, 8)),
            Moon(Vec(3, 5, -1))
        )

        "after one step" - {
            val moonSteps = Day12.updateMoons(moons).take(2).toList()

            moonSteps[0] shouldBe moons
            moonSteps[1] shouldBe Vector.of(
                Moon(Vec(2, -1, 1), Vec(3, -1, -1)),
                Moon(Vec(3, -7, -4), Vec(1, 3, 3)),
                Moon(Vec(1, -7, 5), Vec(-3, 1, -3)),
                Moon(Vec(2, 2, 0), Vec(-1, -3, 1))
            )
        }

        "energy after 10 steps" - {
            val moonSteps = Day12.updateMoons(moons).take(11).toList()
            val moonsAfter10Steps = moonSteps[10]
            moonsAfter10Steps.energy() shouldBe 179
        }

        "repeats after" - {
            val repeatedAfter = Day12.repeatAfter(moons)
            repeatedAfter shouldBe 2772
        }
    }

    "sample data two" - {
        val moons = Vector.of(
            Moon(Vec(-8, -10, 0)),
            Moon(Vec(5, 5, 10)),
            Moon(Vec(2, -7, 3)),
            Moon(Vec(9, -8, -3))
        )

        "repeats after" - {
            val repeatedAfter = Day12.repeatAfter(moons)
            repeatedAfter shouldBe 4686774924L
        }
    }

    "calculate solutions" - {
        val moons = Vector.of(
            Moon(Vec(3, 3, 0)),
            Moon(Vec(4, -16, 2)),
            Moon(Vec(-10, -6, 5)),
            Moon(Vec(-3, 0, -13))
        )

        "energy after 1000 steps (solution one)" - {
            val after1000Steps = Day12.updateMoons(moons)[1000]
            after1000Steps.energy() shouldBe 12351
        }

        "repeats after (solution two)" - {
            val repeatsAfter = Day12.repeatAfter(moons)
            repeatsAfter shouldBe 380635029877596L
        }

    }

})