package ru.fefu.pokeabilityapp.domain.repository

import ru.fefu.pokeabilityapp.domain.model.AbilityItem

interface FavouriteRepository {
    suspend fun getAll(): List<AbilityItem>
    suspend fun add(item: AbilityItem)
    suspend fun remove(id: Int)
}