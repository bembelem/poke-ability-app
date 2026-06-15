package ru.fefu.pokeabilityapp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.fefu.pokeabilityapp.domain.model.AbilityItem
import ru.fefu.pokeabilityapp.domain.repository.FavouriteRepository

class FakeFavouriteRepository : FavouriteRepository {

    private val state = MutableStateFlow<List<AbilityItem>>(emptyList())

    var failGetAll = false

    val favourites: List<AbilityItem>
        get() = state.value

    fun seed(vararg items: AbilityItem) {
        state.value = items.toList()
    }

    override fun observeAll(): Flow<List<AbilityItem>> = state.asStateFlow()

    override suspend fun getAll(): List<AbilityItem> {
        if (failGetAll) error("getAll failed")
        return state.value
    }

    override suspend fun add(item: AbilityItem) {
        state.value = listOf(item) + state.value.filterNot { it.id == item.id }
    }

    override suspend fun remove(id: Int) {
        state.value = state.value.filterNot { it.id == id }
    }
}
