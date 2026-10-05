package ru.fefu.pokeabilityapp.domain.model

const val TEAM_SIZE = 6

data class Team(
    val id: Long,
    val name: String,
    val slots: List<TeamSlot>
) {
    fun slotAt(position: Int): TeamSlot? = slots.firstOrNull { it.position == position }
}

data class TeamSlot(
    val id: Long,
    val position: Int,
    val pokemonId: Int,
    val pokemonName: String,
    val abilityId: Int?,
    val abilityName: String?
)
