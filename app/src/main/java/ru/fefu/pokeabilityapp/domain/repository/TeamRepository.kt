package ru.fefu.pokeabilityapp.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.fefu.pokeabilityapp.domain.model.Team

interface TeamRepository {
    fun observeTeams(): Flow<List<Team>>
    fun observeTeam(teamId: Long): Flow<Team?>

    suspend fun createTeam(name: String): Long
    suspend fun updateTeam(id: Long, name: String, note: String)
    suspend fun deleteTeam(id: Long)

    suspend fun setSlot(teamId: Long, position: Int, pokemonId: Int, pokemonName: String): Long
    suspend fun setSlotAbility(slotId: Long, abilityId: Int?, abilityName: String?)
    suspend fun setSlotDetails(slotId: Long, nickname: String?, note: String?)
    suspend fun clearSlot(teamId: Long, position: Int)
}
