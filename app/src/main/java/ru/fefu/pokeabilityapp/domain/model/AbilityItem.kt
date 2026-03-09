package ru.fefu.pokeabilityapp.domain.model

data class AbilityItem(
    val id: Int,
    val name: String
)

data class AbilityDetail(
    val id: Int,
    val name: String,
    val generation: String,
    val isMainSeries: Boolean,
    val shortEffect: String,
    val fullEffect: String,
    val flavorText: String,
    val pokemonList: List<String>
)
