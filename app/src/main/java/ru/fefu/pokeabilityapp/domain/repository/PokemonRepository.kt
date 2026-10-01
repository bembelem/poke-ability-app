package ru.fefu.pokeabilityapp.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.fefu.pokeabilityapp.domain.model.PokemonDetail
import ru.fefu.pokeabilityapp.domain.model.PokemonItem
import ru.fefu.pokeabilityapp.domain.model.TypeChart

interface PokemonRepository {
    fun observePokemon(): Flow<List<PokemonItem>>
    fun observeDetails(ids: List<Int>): Flow<List<PokemonDetail>>
    fun observeTypeChart(): Flow<TypeChart>

    suspend fun refreshFirstPage(force: Boolean)
    suspend fun loadNextPage(): Int

    /** Догружает типы и способности покемона, если их ещё нет. */
    suspend fun ensureDetail(id: Int)

    suspend fun refreshTypeChart(force: Boolean)
}
