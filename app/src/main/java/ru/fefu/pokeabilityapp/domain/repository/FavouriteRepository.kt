package ru.fefu.pokeabilityapp.domain.repository

import kotlinx.coroutines.flow.Flow

interface FavouriteRepository {
    fun getAll(): Flow<Set<Int>>
    suspend fun add(id: Int)
    suspend fun remove(id: Int)
}