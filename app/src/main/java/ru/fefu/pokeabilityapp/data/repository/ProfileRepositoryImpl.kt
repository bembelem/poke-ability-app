package ru.fefu.pokeabilityapp.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.fefu.pokeabilityapp.data.local.dao.ProfileDao
import ru.fefu.pokeabilityapp.data.local.entity.ProfileEntity
import ru.fefu.pokeabilityapp.data.local.entity.toDomain
import ru.fefu.pokeabilityapp.domain.model.Profile
import ru.fefu.pokeabilityapp.domain.repository.ProfileRepository
import ru.fefu.pokeabilityapp.domain.repository.SettingsRepository
import javax.inject.Inject

class ProfileRepositoryImpl @Inject constructor(
    private val dao: ProfileDao,
    private val settingsRepository: SettingsRepository
) : ProfileRepository {

    override fun observeProfiles(): Flow<List<Profile>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    override suspend fun createProfile(name: String): Long = withContext(Dispatchers.IO) {
        dao.insert(ProfileEntity(name = name))
    }

    override suspend fun deleteProfile(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    override suspend fun ensureActiveProfile(): Long = withContext(Dispatchers.IO) {
        val activeId = settingsRepository.observeSettings().first().activeProfileId
        val profiles = dao.getAll()

        val resolved = profiles.firstOrNull { it.id == activeId }?.id
            ?: profiles.firstOrNull()?.id
            ?: dao.insert(ProfileEntity(name = DEFAULT_PROFILE_NAME))

        if (resolved != activeId) {
            settingsRepository.setActiveProfile(resolved)
        }
        resolved
    }

    private companion object {
        const val DEFAULT_PROFILE_NAME = "Тренер"
    }
}
