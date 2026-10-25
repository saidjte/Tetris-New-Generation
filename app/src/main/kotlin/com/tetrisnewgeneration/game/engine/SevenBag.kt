package com.tetrisnewgeneration.game.engine

import kotlin.random.Random

/**
 * 7-bag randomizer: every bag contains each of the seven pieces exactly once,
 * shuffled. Guarantees a fair piece distribution over time.
 */
class SevenBag(private val random: Random = Random.Default) {

    private var current: MutableList<PieceType> = mutableListOf()

    fun next(): PieceType {
        if (current.isEmpty()) refill()
        return current.removeAt(current.size - 1)
    }

    private fun refill() {
        current = PieceType.entries.toMutableList()
        shuffle(current)
    }

    private fun shuffle(list: MutableList<PieceType>) {
        for (i in list.size - 1 downTo 1) {
            val j = random.nextInt(i + 1)
            val tmp = list[i]
            list[i] = list[j]
            list[j] = tmp
        }
    }
}
