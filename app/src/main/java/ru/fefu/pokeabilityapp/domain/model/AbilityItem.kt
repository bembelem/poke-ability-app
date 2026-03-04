package ru.fefu.pokeabilityapp.domain.model

data class AbilityItem(
    val id: Int,
    val name: String
)

// Для деталей
data class AbilityDetail(
    val id: Int,
    val name: String,
    val generation: String,        // "generation-iii"
    val isMainSeries: Boolean,
    val shortEffect: String,       // из effectEntries, lang=en
    val fullEffect: String,        // из effectEntries, lang=en
    val flavorText: String,        // последний en из flavorTextEntries
    val pokemonList: List<String>  // имена покемонов
)