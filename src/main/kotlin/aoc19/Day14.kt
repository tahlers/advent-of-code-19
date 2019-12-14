package aoc19

import io.vavr.collection.HashMap
import io.vavr.collection.Map
import io.vavr.collection.Vector
import io.vavr.kotlin.tuple
import kotlin.collections.Iterable
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.map

object Day14 {

    private fun <T> Iterable<T>.toVector(): Vector<T> = Vector.ofAll(this)

    data class SubstanceAmounts(val amount: Long, val name: String) {
        companion object {
            fun parse(text: String): SubstanceAmounts {
                val (amount, name) = text.trim().split(" ")
                return SubstanceAmounts(amount.toLong(), name)
            }
        }
        fun multiply(multiplier: Long) = this.copy(amount = this.amount * multiplier)
    }

    data class Reaction(val substance: SubstanceAmounts, val sources: Vector<SubstanceAmounts>) {
        fun neededSourcesWithExcess(amount: Long = substance.amount): Pair<Vector<SubstanceAmounts>, SubstanceAmounts> {
            val mod = amount % substance.amount
            val times = if (mod == 0L) (amount / substance.amount) else (amount / substance.amount) + 1
            val produced = sources.map { it.multiply(times)}
            return produced to substance.copy(amount = substance.amount * times - amount)
        }
    }

    fun parseReactions(reactionsText: String): Map<String, Reaction> {
        val lines = reactionsText.lines()
        val reactions = lines.map { line ->
            val (sourcesText, resultText) = line.split(" => ")
            val result = SubstanceAmounts.parse(resultText)
            val sources = sourcesText.split(", ").map { SubstanceAmounts.parse(it) }.toVector()
            Reaction(result, sources)
        }
        return HashMap.ofEntries(reactions.map { tuple(it.substance.name, it) })
    }

    private fun convertToOreAmount(
        substance: SubstanceAmounts,
        excess: Map<String, SubstanceAmounts>,
        reactions: Map<String, Reaction>
    ): Pair<Long, Map<String, SubstanceAmounts>> {
        return if (substance.name == "ORE") substance.amount to excess else {
            val excessSubstanceAmount = excess[substance.name].map { it.amount }.getOrElse(0L)
            val stillNeeded = substance.amount - excessSubstanceAmount
            if (stillNeeded > 0) {
                val reaction = reactions[substance.name].get()
                val (neededSources, producedExcess) = reaction.neededSourcesWithExcess(stillNeeded)
                val updatedExcess = excess.put(producedExcess.name, producedExcess)

                neededSources.foldLeft(0L to updatedExcess) { (ore, runningExcess), currentSubstance ->
                    val (oreInc, newExcess) = convertToOreAmount(currentSubstance, runningExcess, reactions)
                    ore + oreInc to newExcess
                }
            } else {
                val substanceExcess = substance.copy(amount = excessSubstanceAmount - substance.amount)
                (0L to excess.put(substance.name, substanceExcess ))
            }
        }
    }

    private fun oreForFuel(fuelAmount: Long, reactions: Map<String, Reaction>): Long {
        return convertToOreAmount(SubstanceAmounts(fuelAmount, "FUEL"), HashMap.empty(), reactions).first
    }

    fun fuelForOre(oreAmount: Long, reactions: Map<String, Reaction>): Long {

        tailrec fun search(currentFuel: Long, stepSize: Long): Long {
            val oreForFuel = oreForFuel(currentFuel, reactions)
            return if (oreForFuel < oreAmount && stepSize >  0)  {
                val newStepSize = stepSize * 2
                search(currentFuel + newStepSize, newStepSize )
            } else if (oreForFuel > oreAmount && stepSize < 0) {
                search(currentFuel + stepSize, stepSize)
            } else if (oreForFuel < oreAmount && stepSize < 0) {
                if (stepSize == -1L) currentFuel else {
                    val newStepSize = stepSize / 2 * -1
                    search(currentFuel + newStepSize, newStepSize)
                }
            } else {
                if (stepSize == 1L) currentFuel -1 else {
                    val newStepSize = stepSize / 2 * -1
                    search(currentFuel + newStepSize, newStepSize)
                }
            }
        }

        return search(1L, 2)
    }

    fun calculateOreForFuel(reactionsText: String): Long {
        val reactions = parseReactions(reactionsText)
        return oreForFuel(1L, reactions)
    }
}