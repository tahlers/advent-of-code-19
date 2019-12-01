package aoc19

object Day1 {

    fun calculateFuel(moduleWeight: Int): Int {
        val divided = moduleWeight.div(3)
        return divided - 2
    }

    fun calculateFuelByModuleWeightStrings(moduleWeights: List<String>): Int {
        return moduleWeights
            .map { calculateFuel(it.toInt()) }
            .sum()
    }

    fun calculateFuel2(weight: Int, carryover: Int = 0): Int{
        val fuel = calculateFuel(weight)
        return if (fuel <= 0 ) {
            carryover
        } else {
            calculateFuel2(fuel, fuel + carryover)
        }
    }

    fun calculateFuel2ByModuleWeightStrings(moduleWeights: List<String>): Int {
        return moduleWeights
            .map { calculateFuel2(it.toInt()) }
            .sum()
    }

}