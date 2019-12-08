package aoc19

import io.vavr.collection.Vector

typealias Layer = Vector<Vector<Int>>

fun <T> Iterable<T>.toVector(): Vector<T> = Vector.ofAll(this)

object Day8 {

    fun parseImage(digits: Vector<Int>, width: Int, height: Int): Vector<Layer> {
        val layers = digits.grouped(width).toVector().grouped(height)
        return layers.toVector()
    }

    private fun countInLayer(digit: Int, layer: Layer): Int {
        return layer.map { it.count { i -> i == digit } }.sum().toInt()
    }

    fun findFewest0DigitsChecksum(layers: Vector<Layer>): Int {
        val countMap = layers.map { layer ->
            val zeroCount = countInLayer(0, layer)
            zeroCount to layer
        }.toMap()
        val lowest = countMap.keys.min()
        val lowestLayer = countMap.getOrDefault(lowest, Vector.empty())
        val countOnes = countInLayer(1, lowestLayer)
        val countTwos = countInLayer(2, lowestLayer)

        return countOnes * countTwos
    }

    fun producePicture(layers: Vector<Layer>): Layer {
        val width = layers[0][0].size()
        val height = layers[0].size()
        val picture = (0 until height).map { h ->
            (0 until width).map { w ->
                val layerPixels = layers.map { it[h][w] }
                layerPixels.first { it != 2 }
            }.toVector()
        }.toVector()
        printPicture(picture)
        return picture
    }

    private fun printPicture(picture: Layer) {
        val width = picture[0].size() + 2
        println("█".repeat(width))
        picture.forEach { row ->
            print("█")
            row.forEach { pixel ->
                if (pixel == 0) print("█") else print(" ")
            }
            print("█\n")
        }
        println("█".repeat(width))
    }
}