package ru.fefu.pokeabilityapp.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.fefu.pokeabilityapp.domain.model.AbilityDetail
import ru.fefu.pokeabilityapp.domain.model.AbilityItem

interface AbilityRepository {
    fun observeAbilities(): Flow<List<AbilityItem>>
    fun observeAbilityDetail(id: Int): Flow<AbilityDetail?>

    /** Обновляет первую страницу, если кэш устарел. force игнорирует TTL. */
    suspend fun refreshFirstPage(force: Boolean)

    /** Догружает следующую страницу, возвращает число полученных записей. */
    suspend fun loadNextPage(): Int

    suspend fun refreshDetail(id: Int, force: Boolean)

    /** Сначала ищет в кэше, если там нет, запрашивает сеть. */
    suspend fun findByName(name: String): AbilityItem?
}
