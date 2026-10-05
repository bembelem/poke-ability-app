package ru.fefu.pokeabilityapp.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import ru.fefu.pokeabilityapp.data.local.AbilityCacheDao
import ru.fefu.pokeabilityapp.data.local.toDetailOrNull
import ru.fefu.pokeabilityapp.data.local.toItem
import ru.fefu.pokeabilityapp.data.mapper.toCached
import ru.fefu.pokeabilityapp.data.mapper.toCachedOrNull
import ru.fefu.pokeabilityapp.data.service.PokeApiService
import ru.fefu.pokeabilityapp.domain.model.AbilityDetail
import ru.fefu.pokeabilityapp.domain.model.AbilityItem
import ru.fefu.pokeabilityapp.domain.model.hoursToMillis
import ru.fefu.pokeabilityapp.domain.model.isStale
import ru.fefu.pokeabilityapp.domain.repository.AbilityRepository
import ru.fefu.pokeabilityapp.domain.repository.SettingsRepository
import javax.inject.Inject

const val ABILITY_PAGE_SIZE = 20

class AbilityRepositoryImpl @Inject constructor(
    private val api: PokeApiService,
    private val dao: AbilityCacheDao,
    private val settingsRepository: SettingsRepository
) : AbilityRepository {

    override fun observeAbilities(): Flow<List<AbilityItem>> =
        dao.observeList().map { list -> list.map { it.toItem() } }

    override fun observeAbilityDetail(id: Int): Flow<AbilityDetail?> =
        dao.observeById(id).map { it?.toDetailOrNull() }

    override suspend fun refreshFirstPage(force: Boolean) = withContext(Dispatchers.IO) {
        val fresh = !force &&
            dao.listCount() > 0 &&
            !isStale(dao.oldestListFetchedAt(), ttlMillis(), System.currentTimeMillis())
        if (fresh) return@withContext

        val now = System.currentTimeMillis()
        val response = api.getAbilities(limit = ABILITY_PAGE_SIZE, offset = 0)
        dao.saveListEntries(
            response.results.mapIndexedNotNull { index, entry ->
                entry.toCachedOrNull(listOrder = index, fetchedAt = now)
            }
        )
    }

    override suspend fun loadNextPage(): Int = withContext(Dispatchers.IO) {
        val offset = dao.listCount()
        val now = System.currentTimeMillis()
        val response = api.getAbilities(limit = ABILITY_PAGE_SIZE, offset = offset)
        val entries = response.results.mapIndexedNotNull { index, entry ->
            entry.toCachedOrNull(listOrder = offset + index, fetchedAt = now)
        }
        dao.saveListEntries(entries)
        entries.size
    }

    override suspend fun refreshDetail(id: Int, force: Boolean) = withContext(Dispatchers.IO) {
        val cached = dao.getById(id)
        val fresh = !force &&
            cached?.detailFetchedAt != null &&
            !isStale(cached.detailFetchedAt, ttlMillis(), System.currentTimeMillis())
        if (fresh) return@withContext

        val dto = api.getAbilityById(id)
        dao.save(dto.toCached(cached, System.currentTimeMillis()))
    }

    override suspend fun findByName(name: String): AbilityItem? = withContext(Dispatchers.IO) {
        val key = name.trim().lowercase()
        val cached = dao.getByName(key)
        if (cached != null) return@withContext cached.toItem()

        try {
            val dto = api.getAbilityByName(key)
            val entity = dto.toCached(dao.getById(dto.id), System.currentTimeMillis())
            dao.save(entity)
            entity.toItem()
        } catch (e: HttpException) {
            if (e.code() == 404) null else throw e
        }
    }

    private suspend fun ttlMillis(): Long =
        hoursToMillis(settingsRepository.observeSettings().first().cacheTtlHours)
}
