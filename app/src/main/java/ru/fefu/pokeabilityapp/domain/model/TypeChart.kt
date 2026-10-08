package ru.fefu.pokeabilityapp.domain.model

class TypeChart(private val multipliers: Map<Pair<PokeType, PokeType>, Double>) {

    val isEmpty: Boolean
        get() = multipliers.isEmpty()

    fun multiplier(attacking: PokeType, defending: PokeType): Double =
        multipliers[attacking to defending] ?: NEUTRAL

    companion object {
        const val NEUTRAL = 1.0
        val EMPTY = TypeChart(emptyMap())
    }
}
