package ru.fefu.pokeabilityapp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import ru.fefu.pokeabilityapp.domain.model.Team
import ru.fefu.pokeabilityapp.domain.model.TeamSlot
import ru.fefu.pokeabilityapp.domain.repository.TeamRepository

class FakeTeamRepository(initial: Team) : TeamRepository {

    val team = MutableStateFlow<Team?>(initial)

    override fun observeTeams(): Flow<List<Team>> = team.map { listOfNotNull(it) }

    override fun observeTeam(teamId: Long): Flow<Team?> = team

    override suspend fun createTeam(name: String): Long = error("not used in tests")

    override suspend fun updateTeam(id: Long, name: String, note: String) {
        team.update { it?.copy(name = name, note = note) }
    }

    override suspend fun deleteTeam(id: Long) {
        team.value = null
    }

    override suspend fun setSlot(
        teamId: Long,
        position: Int,
        pokemonId: Int,
        pokemonName: String
    ): Long = error("not used in tests")

    override suspend fun setSlotAbility(slotId: Long, abilityId: Int?, abilityName: String?) {
        updateSlot(slotId) { it.copy(abilityId = abilityId, abilityName = abilityName) }
    }

    override suspend fun setSlotDetails(slotId: Long, nickname: String?, note: String?) {
        updateSlot(slotId) { it.copy(nickname = nickname, note = note) }
    }

    override suspend fun clearSlot(teamId: Long, position: Int) {
        team.update { current ->
            current?.copy(slots = current.slots.filterNot { it.position == position })
        }
    }

    private fun updateSlot(slotId: Long, change: (TeamSlot) -> TeamSlot) {
        team.update { current ->
            current?.copy(slots = current.slots.map { if (it.id == slotId) change(it) else it })
        }
    }
}
