package ru.fefu.pokeabilityapp.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.fefu.pokeabilityapp.data.local.dao.TeamDao
import ru.fefu.pokeabilityapp.data.local.entity.TeamEntity
import ru.fefu.pokeabilityapp.data.local.entity.TeamSlotEntity
import ru.fefu.pokeabilityapp.data.local.entity.toDomain
import ru.fefu.pokeabilityapp.domain.model.Team
import ru.fefu.pokeabilityapp.domain.repository.ProfileRepository
import ru.fefu.pokeabilityapp.domain.repository.SettingsRepository
import ru.fefu.pokeabilityapp.domain.repository.TeamRepository
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class TeamRepositoryImpl @Inject constructor(
    private val dao: TeamDao,
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository
) : TeamRepository {

    override fun observeTeams(): Flow<List<Team>> =
        settingsRepository.observeSettings()
            .map { it.activeProfileId }
            .distinctUntilChanged()
            .flatMapLatest { profileId ->
                dao.observeTeams(profileId).map { list -> list.map { it.toDomain() } }
            }

    override fun observeTeam(teamId: Long): Flow<Team?> =
        dao.observeTeam(teamId).map { it?.toDomain() }

    override suspend fun createTeam(name: String): Long = withContext(Dispatchers.IO) {
        val profileId = profileRepository.ensureActiveProfile()
        dao.insertTeam(TeamEntity(profileId = profileId, name = name))
    }

    override suspend fun deleteTeam(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteTeam(id)
    }

    override suspend fun setSlot(
        teamId: Long,
        position: Int,
        pokemonId: Int,
        pokemonName: String
    ): Long = withContext(Dispatchers.IO) {
        val existing = dao.getSlotAt(teamId, position)
        val slotId = if (existing == null) {
            dao.insertSlot(
                TeamSlotEntity(
                    teamId = teamId,
                    position = position,
                    pokemonId = pokemonId,
                    pokemonName = pokemonName
                )
            )
        } else {
            // способность принадлежит конкретному покемону, при замене она сбрасывается
            val samePokemon = existing.pokemonId == pokemonId
            dao.updateSlot(
                existing.copy(
                    pokemonId = pokemonId,
                    pokemonName = pokemonName,
                    abilityId = if (samePokemon) existing.abilityId else null,
                    abilityName = if (samePokemon) existing.abilityName else null
                )
            )
            existing.id
        }
        dao.touchTeam(teamId, System.currentTimeMillis())
        slotId
    }

    override suspend fun setSlotAbility(slotId: Long, abilityId: Int?, abilityName: String?) =
        withContext(Dispatchers.IO) {
            val slot = dao.getSlot(slotId) ?: return@withContext
            dao.updateSlot(slot.copy(abilityId = abilityId, abilityName = abilityName))
            dao.touchTeam(slot.teamId, System.currentTimeMillis())
        }

    override suspend fun clearSlot(teamId: Long, position: Int) = withContext(Dispatchers.IO) {
        dao.deleteSlotAt(teamId, position)
        dao.touchTeam(teamId, System.currentTimeMillis())
    }
}
