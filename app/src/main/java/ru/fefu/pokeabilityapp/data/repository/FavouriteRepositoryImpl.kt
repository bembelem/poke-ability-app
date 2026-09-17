package ru.fefu.pokeabilityapp.data.repository

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import ru.fefu.pokeabilityapp.data.local.FavouriteDao
import ru.fefu.pokeabilityapp.data.local.FavouriteEntity
import ru.fefu.pokeabilityapp.data.local.toDomain
import ru.fefu.pokeabilityapp.domain.model.AbilityItem
import ru.fefu.pokeabilityapp.domain.repository.FavouriteRepository
import ru.fefu.pokeabilityapp.domain.repository.ProfileRepository
import ru.fefu.pokeabilityapp.domain.repository.SettingsRepository
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class FavouriteRepositoryImpl @Inject constructor(
    private val dao: FavouriteDao,
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository
) : FavouriteRepository {

    override suspend fun getAll(): List<AbilityItem> {
        val profileId = profileRepository.ensureActiveProfile()
        return dao.getAll(profileId).map { it.toDomain() }
    }

    override suspend fun add(item: AbilityItem) {
        val profileId = profileRepository.ensureActiveProfile()
        dao.insert(FavouriteEntity(profileId = profileId, id = item.id, name = item.name))
    }

    override suspend fun remove(id: Int) {
        val profileId = profileRepository.ensureActiveProfile()
        dao.deleteById(profileId, id)
    }

    override fun observeAll(): Flow<List<AbilityItem>> =
        settingsRepository.observeSettings()
            .map { it.activeProfileId }
            .distinctUntilChanged()
            .flatMapLatest { profileId ->
                dao.observeAll(profileId).map { list -> list.map { it.toDomain() } }
            }
}
