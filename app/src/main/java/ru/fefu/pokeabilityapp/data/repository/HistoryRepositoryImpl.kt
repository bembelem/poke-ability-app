package ru.fefu.pokeabilityapp.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.fefu.pokeabilityapp.data.local.HistoryDao
import ru.fefu.pokeabilityapp.data.local.HistoryEntryEntity
import ru.fefu.pokeabilityapp.data.local.toDomain
import ru.fefu.pokeabilityapp.domain.model.HistoryEntry
import ru.fefu.pokeabilityapp.domain.repository.HistoryRepository
import ru.fefu.pokeabilityapp.domain.repository.ProfileRepository
import ru.fefu.pokeabilityapp.domain.repository.SettingsRepository
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryRepositoryImpl @Inject constructor(
    private val dao: HistoryDao,
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository
) : HistoryRepository {

    override fun observeRecent(): Flow<List<HistoryEntry>> =
        settingsRepository.observeSettings()
            .map { it.activeProfileId }
            .distinctUntilChanged()
            .flatMapLatest { profileId ->
                dao.observeAll(profileId).map { list -> list.map { it.toDomain() } }
            }

    override suspend fun record(abilityId: Int, abilityName: String) = withContext(Dispatchers.IO) {
        if (!settingsRepository.observeSettings().first().historyEnabled) return@withContext

        val profileId = profileRepository.ensureActiveProfile()
        // одна запись на способность, повторный просмотр поднимает её наверх
        dao.deleteFor(profileId, abilityId)
        dao.insert(
            HistoryEntryEntity(
                profileId = profileId,
                abilityId = abilityId,
                abilityName = abilityName,
                viewedAt = System.currentTimeMillis()
            )
        )
    }

    override suspend fun clear() = withContext(Dispatchers.IO) {
        dao.clear(profileRepository.ensureActiveProfile())
    }
}
