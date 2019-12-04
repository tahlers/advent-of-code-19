package aoc19

object Day4 {

    fun isValid(s: String): Boolean {
        val containsDouble = s.toSet().size != s.length
        val noDecrease = isNotDecreasing(s)
        return containsDouble && noDecrease
    }

    fun isValid2(s: String): Boolean {
        val hasAtLeastOneSingleDouble = hasAtLeastOneSingleDouble(s)
        val noDecrease = isNotDecreasing(s)
        return hasAtLeastOneSingleDouble && noDecrease
    }

    private fun isNotDecreasing(s: String): Boolean {
        return s.fold(-1) { incStatus, char ->
            val newDigit = char.toInt()
            if (newDigit < incStatus) return false else newDigit
        }.let { true }
    }

    private fun hasAtLeastOneSingleDouble(s: String): Boolean {

        data class DoubleStatus(val c: Char, val count: Int)

        val folded = s.fold(DoubleStatus(s[0], 0)) { status, char ->
            if (status.c != char && status.count == 2) return true
            if (status.c == char) {
                DoubleStatus(char, status.count + 1)
            } else {
                DoubleStatus(char, 1)
            }
        }

        return folded.count == 2
    }

    fun numberOfValidCombinations(start: String, end: String): Int {
        val startNum = start.toInt()
        val endNum = end.toInt()

        return (startNum..(endNum + 1)).filter { isValid(it.toString()) }.size
    }

    fun numberOfValidCombinations2(start: String, end: String): Int {
        val startNum = start.toInt()
        val endNum = end.toInt()

        return (startNum..(endNum + 1)).filter { isValid2(it.toString()) }.size
    }
}