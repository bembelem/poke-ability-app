package ru.fefu.pokeabilityapp.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.fefu.pokeabilityapp.domain.model.HistoryEntry

interface HistoryRepository {
    fun observeRecent(): Flow<List<HistoryEntry>>
    suspend fun record(abilityId: Int, abilityName: String)
    suspend fun clear()
}
