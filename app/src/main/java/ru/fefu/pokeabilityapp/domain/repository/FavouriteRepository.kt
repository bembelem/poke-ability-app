package ru.fefu.pokeabilityapp.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.fefu.pokeabilityapp.domain.model.AbilityItem

interface FavouriteRepository {
    fun observeAll(): Flow<List<AbilityItem>>
    suspend fun getAll(): List<AbilityItem>
    suspend fun add(item: AbilityItem)
    suspend fun remove(id: Int)
}