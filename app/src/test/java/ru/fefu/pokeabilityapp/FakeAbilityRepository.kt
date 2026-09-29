package ru.fefu.pokeabilityapp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.fefu.pokeabilityapp.domain.model.AbilityDetail
import ru.fefu.pokeabilityapp.domain.model.AbilityItem
import ru.fefu.pokeabilityapp.domain.repository.AbilityRepository

class FakeAbilityRepository : AbilityRepository {

    private val cached = MutableStateFlow<List<AbilityItem>>(emptyList())
    private val detail = MutableStateFlow<AbilityDetail?>(null)

    var refreshResult: List<AbilityItem> = emptyList()
    var nextPage: List<AbilityItem> = emptyList()
    var searchResult: AbilityItem? = null
    var detailResult: AbilityDetail? = null

    var failRefresh = false
    var failLoadMore = false
    var failSearch = false
    var failDetail = false

    var refreshCalls = 0
        private set

    fun seedCache(vararg items: AbilityItem) {
        cached.value = items.toList()
    }

    override fun observeAbilities(): Flow<List<AbilityItem>> = cached.asStateFlow()

    override fun observeAbilityDetail(id: Int): Flow<AbilityDetail?> = detail.asStateFlow()

    override suspend fun refreshFirstPage(force: Boolean) {
        refreshCalls++
        if (failRefresh) error("refresh failed")
        cached.value = refreshResult
    }

    override suspend fun loadNextPage(): Int {
        if (failLoadMore) error("load next page failed")
        cached.value = cached.value + nextPage
        return nextPage.size
    }

    override suspend fun refreshDetail(id: Int, force: Boolean) {
        if (failDetail) error("detail failed")
        detail.value = detailResult
    }

    override suspend fun findByName(name: String): AbilityItem? {
        if (failSearch) error("search failed")
        return searchResult
    }
}
