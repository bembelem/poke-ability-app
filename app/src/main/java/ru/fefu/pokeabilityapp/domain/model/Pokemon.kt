package ru.fefu.pokeabilityapp.domain.model

data class PokemonItem(
    val id: Int,
    val name: String,
    val spriteUrl: String?
)

data class PokemonAbilityOption(
    val abilityId: Int,
    val name: String,
    val isHidden: Boolean
)

data class PokemonDetail(
    val id: Int,
    val name: String,
    val spriteUrl: String?,
    val types: List<PokeType>,
    val abilities: List<PokemonAbilityOption>
)
