package ru.fefu.pokeabilityapp.domain.model

data class HistoryEntry(
    val abilityId: Int,
    val abilityName: String,
    val viewedAt: Long
)
