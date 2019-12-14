package aoc19

import aoc19.Day14.Reaction
import aoc19.Day14.SubstanceAmounts
import io.kotlintest.shouldBe
import io.kotlintest.specs.FreeSpec
import io.vavr.collection.HashMap
import io.vavr.collection.Vector

class Day14Test : FreeSpec({

    val oreAvailable = 1000000000000L

    "first example" - {
        val data = """
            9 ORE => 2 A
            8 ORE => 3 B
            7 ORE => 5 C
            3 A, 4 B => 1 AB
            5 B, 7 C => 1 BC
            4 C, 1 A => 1 CA
            2 AB, 3 BC, 4 CA => 1 FUEL
        """.trimIndent()

        "should be parsed to reactions map" {
            val result = Day14.parseReactions(data)
            val expected = HashMap.of(
                "A",
                Reaction(SubstanceAmounts(2, "A"), Vector.of(SubstanceAmounts(9, "ORE"))),
                "B",
                Reaction(SubstanceAmounts(3, "B"), Vector.of(SubstanceAmounts(8, "ORE"))),
                "C",
                Reaction(SubstanceAmounts(5, "C"), Vector.of(SubstanceAmounts(7, "ORE"))),
                "AB",
                Reaction(SubstanceAmounts(1, "AB"), Vector.of(SubstanceAmounts(3, "A"), SubstanceAmounts(4, "B"))),
                "BC",
                Reaction(SubstanceAmounts(1, "BC"), Vector.of(SubstanceAmounts(5, "B"), SubstanceAmounts(7, "C"))),
                "CA",
                Reaction(SubstanceAmounts(1, "CA"), Vector.of(SubstanceAmounts(4, "C"), SubstanceAmounts(1, "A"))),
                "FUEL",
                Reaction(SubstanceAmounts(1, "FUEL"), Vector.of(SubstanceAmounts(2, "AB"), SubstanceAmounts(3, "BC"), SubstanceAmounts(4, "CA")))
            )

            result.keySet() shouldBe expected.keySet()
            result["A"] shouldBe expected["A"]
            result["B"] shouldBe expected["B"]
            result["C"] shouldBe expected["C"]
            result["AB"] shouldBe expected["AB"]
            result["BC"] shouldBe expected["BC"]
            result["CA"] shouldBe expected["CA"]
            result["FUEL"] shouldBe expected["FUEL"]
        }

        "should calculate ORE need" {
            val result = Day14.calculateOreForFuel(data)
            result shouldBe 165L
        }

    }

    "more examples" - {
        "example 2" - {
            val data = """
                157 ORE => 5 NZVS
                165 ORE => 6 DCFZ
                44 XJWVT, 5 KHKGT, 1 QDVJ, 29 NZVS, 9 GPVTF, 48 HKGWZ => 1 FUEL
                12 HKGWZ, 1 GPVTF, 8 PSHF => 9 QDVJ
                179 ORE => 7 PSHF
                177 ORE => 5 HKGWZ
                7 DCFZ, 7 PSHF => 2 XJWVT
                165 ORE => 2 GPVTF
                3 DCFZ, 7 NZVS, 5 HKGWZ, 10 PSHF => 8 KHKGT
            """.trimIndent()
            val reactions = Day14.parseReactions(data)

            "example2 ore needed" {
                val result = Day14.calculateOreForFuel(data)
                result shouldBe 13312L
            }

            "example2 fuel with ore" {
                val result = Day14.fuelForOre(oreAvailable, reactions)
                result shouldBe 82892753L
            }
        }


        "example 3" - {
            val data = """
                2 VPVL, 7 FWMGM, 2 CXFTF, 11 MNCFX => 1 STKFG
                17 NVRVD, 3 JNWZP => 8 VPVL
                53 STKFG, 6 MNCFX, 46 VJHF, 81 HVMC, 68 CXFTF, 25 GNMV => 1 FUEL
                22 VJHF, 37 MNCFX => 5 FWMGM
                139 ORE => 4 NVRVD
                144 ORE => 7 JNWZP
                5 MNCFX, 7 RFSQX, 2 FWMGM, 2 VPVL, 19 CXFTF => 3 HVMC
                5 VJHF, 7 MNCFX, 9 VPVL, 37 CXFTF => 6 GNMV
                145 ORE => 6 MNCFX
                1 NVRVD => 8 CXFTF
                1 VJHF, 6 MNCFX => 4 RFSQX
                176 ORE => 6 VJHF
            """.trimIndent()
            val reactions = Day14.parseReactions(data)

            "example3 ore needed" {
                val result = Day14.calculateOreForFuel(data)
                result shouldBe 180697L
            }

            "example3 fuel with ore" {
                val result = Day14.fuelForOre(oreAvailable, reactions)
                result shouldBe 5586022L
            }
        }

        "example 4" - {
            val data = """
                171 ORE => 8 CNZTR
                7 ZLQW, 3 BMBT, 9 XCVML, 26 XMNCP, 1 WPTQ, 2 MZWV, 1 RJRHP => 4 PLWSL
                114 ORE => 4 BHXH
                14 VRPVC => 6 BMBT
                6 BHXH, 18 KTJDG, 12 WPTQ, 7 PLWSL, 31 FHTLT, 37 ZDVW => 1 FUEL
                6 WPTQ, 2 BMBT, 8 ZLQW, 18 KTJDG, 1 XMNCP, 6 MZWV, 1 RJRHP => 6 FHTLT
                15 XDBXC, 2 LTCX, 1 VRPVC => 6 ZLQW
                13 WPTQ, 10 LTCX, 3 RJRHP, 14 XMNCP, 2 MZWV, 1 ZLQW => 1 ZDVW
                5 BMBT => 4 WPTQ
                189 ORE => 9 KTJDG
                1 MZWV, 17 XDBXC, 3 XCVML => 2 XMNCP
                12 VRPVC, 27 CNZTR => 2 XDBXC
                15 KTJDG, 12 BHXH => 5 XCVML
                3 BHXH, 2 VRPVC => 7 MZWV
                121 ORE => 7 VRPVC
                7 XCVML => 6 RJRHP
                5 BHXH, 4 VRPVC => 5 LTCX
            """.trimIndent()
            val reactions = Day14.parseReactions(data)

            "example4 ore needed" {
                val result = Day14.calculateOreForFuel(data)
                result shouldBe 2210736L
            }

            "example4 fuel with ore" {
                val result = Day14.fuelForOre(oreAvailable, reactions)
                result shouldBe 460664L
            }
        }
    }

    "calculate solution one" {
        val data = this.javaClass.getResource("/day14_1.txt").readText()
        val result = Day14.calculateOreForFuel(data)
        result shouldBe 892207L
    }

    "calculate solution two" {
        val data = this.javaClass.getResource("/day14_1.txt").readText()
        val reactions = Day14.parseReactions(data)
        val result = Day14.fuelForOre(oreAvailable, reactions)
        result shouldBe 1935265L

    }

})