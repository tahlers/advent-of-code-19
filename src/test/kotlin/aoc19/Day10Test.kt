package aoc19

import aoc19.Day10.Asteroid
import io.kotlintest.matchers.collections.shouldContainAll
import io.kotlintest.matchers.collections.shouldHaveSize
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec

class Day10Test : FreeSpec({

    "sample data" - {
        val fieldString = """
            .#..#
            .....
            #####
            ....#
            ...##
        """.trimIndent()

        "should parse field" - {
            val field = Day10.parseField(fieldString).toJavaSet()
            field shouldHaveSize 10
            field.shouldContainAll(Asteroid(1, 0), Asteroid(0, 2), Asteroid(3, 4), Asteroid(4, 4))
        }

        "should compute observable asteroids" - {
            val field = Day10.parseField(fieldString)
            val result = Day10.computeObservableMap(field)
            result.get(Asteroid(1, 0)).getOrElse(0) shouldBe 7
            result.get(Asteroid(0, 2)).getOrElse(0) shouldBe 6
            result.get(Asteroid(3, 4)).getOrElse(0) shouldBe 8
            result.get(Asteroid(4, 4)).getOrElse(0) shouldBe 7
        }

        "should find largest count in field" - {
            val field = Day10.parseField(fieldString)
            val map = Day10.computeObservableMap(field)
            val result = Day10.findLargestCountInObservableMap(map)
            result._2 shouldBe 8
        }
    }

    "larger examples" - {
        "example1" - {
            val fieldString = """
            ......#.#.
            #..#.#....
            ..#######.
            .#.#.###..
            .#..#.....
            ..#....#.#
            #..#....#.
            .##.#..###
            ##...#..#.
            .#....####
        """.trimIndent()
            val field = Day10.parseField(fieldString)
            val map = Day10.computeObservableMap(field)
            val result = Day10.findLargestCountInObservableMap(map)
            result._2 shouldBe 33
        }

        "example2" - {
            val fieldString = """
            #.#...#.#.
            .###....#.
            .#....#...
            ##.#.#.#.#
            ....#.#.#.
            .##..###.#
            ..#...##..
            ..##....##
            ......#...
            .####.###.
        """.trimIndent()
            val field = Day10.parseField(fieldString)
            val map = Day10.computeObservableMap(field)
            val result = Day10.findLargestCountInObservableMap(map)
            result._2 shouldBe 35
        }

        "example3" - {
            val fieldString = """
            .#..#..###
            ####.###.#
            ....###.#.
            ..###.##.#
            ##.##.#.#.
            ....###..#
            ..#.#..#.#
            #..#.#.###
            .##...##.#
            .....#.#..
        """.trimIndent()
            val field = Day10.parseField(fieldString)
            val map = Day10.computeObservableMap(field)
            val result = Day10.findLargestCountInObservableMap(map)
            result._2 shouldBe 41
        }

        "example4" - {
            val fieldString = """
            .#..##.###...#######
            ##.############..##.
            .#.######.########.#
            .###.#######.####.#.
            #####.##.#.##.###.##
            ..#####..#.#########
            ####################
            #.####....###.#.#.##
            ##.#################
            #####.##.###..####..
            ..######..##.#######
            ####.##.####...##..#
            .#####..#.######.###
            ##...#.##########...
            #.##########.#######
            .####.#.###.###.#.##
            ....##.##.###..#####
            .#.#.###########.###
            #.#.#.#####.####.###
            ###.##.####.##.#..##
        """.trimIndent()
            val field = Day10.parseField(fieldString)
            val map = Day10.computeObservableMap(field)
            val result = Day10.findLargestCountInObservableMap(map)
            result._2 shouldBe 210

            "200. vaporized " - {
                val station = Asteroid(11, 13)
                val resultVaporized = Day10.findVaporizedAsteroid(station, field, 200)
                resultVaporized shouldBe Asteroid(8, 2)
            }
        }
    }

    "calculate solution one".config(enabled = true) {
        val fieldString = this.javaClass.getResource("/day10_1.txt").readText()
        val field = Day10.parseField(fieldString)
        val map = Day10.computeObservableMap(field)
        val result = Day10.findLargestCountInObservableMap(map)
        result._1 shouldBe Asteroid(37, 25)
        result._2 shouldBe 309
    }

    "calculate solution two".config(enabled = true) {
        val fieldString = this.javaClass.getResource("/day10_1.txt").readText()
        val field = Day10.parseField(fieldString)
        val station = Asteroid(37, 25)
        val resultAsteroid = Day10.findVaporizedAsteroid(station, field, 200)
        resultAsteroid shouldBe Asteroid(x=4, y=16)
        val answer = resultAsteroid.x * 100 + resultAsteroid.y
        answer shouldBe 416

    }

})