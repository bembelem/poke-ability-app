package ru.fefu.pokeabilityapp.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.fefu.pokeabilityapp.data.local.dao.PokemonCacheDao
import ru.fefu.pokeabilityapp.data.mapper.toAbilityEntities
import ru.fefu.pokeabilityapp.data.mapper.toCached
import ru.fefu.pokeabilityapp.data.mapper.toCachedPokemonOrNull
import ru.fefu.pokeabilityapp.data.mapper.toDetail
import ru.fefu.pokeabilityapp.data.mapper.toItem
import ru.fefu.pokeabilityapp.data.mapper.toRelations
import ru.fefu.pokeabilityapp.data.service.PokeApiService
import ru.fefu.pokeabilityapp.domain.model.PokeType
import ru.fefu.pokeabilityapp.domain.model.PokemonDetail
import ru.fefu.pokeabilityapp.domain.model.PokemonItem
import ru.fefu.pokeabilityapp.domain.model.TypeChart
import ru.fefu.pokeabilityapp.domain.model.hoursToMillis
import ru.fefu.pokeabilityapp.domain.model.isStale
import ru.fefu.pokeabilityapp.domain.repository.PokemonRepository
import ru.fefu.pokeabilityapp.domain.repository.SettingsRepository
import javax.inject.Inject

const val POKEMON_PAGE_SIZE = 30

// в PokeAPI основные типы занимают id с 1 по 18
private const val LAST_TYPE_ID = 18

class PokemonRepositoryImpl @Inject constructor(
    private val api: PokeApiService,
    private val dao: PokemonCacheDao,
    private val settingsRepository: SettingsRepository
) : PokemonRepository {

    override fun observePokemon(): Flow<List<PokemonItem>> =
        dao.observeList().map { list -> list.map { it.toItem() } }

    override fun observeDetails(ids: List<Int>): Flow<List<PokemonDetail>> =
        combine(
            dao.observeByIds(ids),
            dao.observeAbilitiesFor(ids)
        ) { pokemon, abilities ->
            val byPokemon = abilities.groupBy { it.pokemonId }
            pokemon.map { it.toDetail(byPokemon[it.id].orEmpty()) }
        }

    override fun observeTypeChart(): Flow<TypeChart> =
        dao.observeTypeChart().map { rows ->
            TypeChart(
                rows.mapNotNull { row ->
                    val attacking = PokeType.fromApiName(row.attacking) ?: return@mapNotNull null
                    val defending = PokeType.fromApiName(row.defending) ?: return@mapNotNull null
                    (attacking to defending) to row.multiplier
                }.toMap()
            )
        }

    override suspend fun refreshFirstPage(force: Boolean) = withContext(Dispatchers.IO) {
        val fresh = !force &&
            dao.listCount() > 0 &&
            !isStale(dao.oldestListFetchedAt(), ttlMillis(), System.currentTimeMillis())
        if (fresh) return@withContext

        val now = System.currentTimeMillis()
        val response = api.getPokemonList(limit = POKEMON_PAGE_SIZE, offset = 0)
        dao.saveListEntries(
            response.results.mapIndexedNotNull { index, entry ->
                entry.toCachedPokemonOrNull(listOrder = index, fetchedAt = now)
            }
        )
    }

    override suspend fun loadNextPage(): Int = withContext(Dispatchers.IO) {
        val offset = dao.listCount()
        val now = System.currentTimeMillis()
        val response = api.getPokemonList(limit = POKEMON_PAGE_SIZE, offset = offset)
        val entries = response.results.mapIndexedNotNull { index, entry ->
            entry.toCachedPokemonOrNull(listOrder = offset + index, fetchedAt = now)
        }
        dao.saveListEntries(entries)
        entries.size
    }

    override suspend fun ensureDetail(id: Int) = withContext(Dispatchers.IO) {
        val cached = dao.getById(id)
        val fresh = cached?.detailFetchedAt != null &&
            !isStale(cached.detailFetchedAt, ttlMillis(), System.currentTimeMillis())
        if (fresh) return@withContext

        val dto = api.getPokemonById(id)
        val now = System.currentTimeMillis()
        dao.save(dto.toCached(cached, now))
        dao.deleteAbilitiesFor(id)
        dao.insertAbilities(dto.toAbilityEntities())
    }

    override suspend fun refreshTypeChart(force: Boolean) = withContext(Dispatchers.IO) {
        val fresh = !force &&
            dao.typeRelationCount() > 0 &&
            !isStale(dao.oldestTypeFetchedAt(), ttlMillis(), System.currentTimeMillis())
        if (fresh) return@withContext

        val now = System.currentTimeMillis()
        val relations = (1..LAST_TYPE_ID).flatMap { api.getType(it).toRelations(now) }
        dao.insertTypeRelations(relations)
    }

    private suspend fun ttlMillis(): Long =
        hoursToMillis(settingsRepository.observeSettings().first().cacheTtlHours)
}
