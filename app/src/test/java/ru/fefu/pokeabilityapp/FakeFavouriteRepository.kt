package ru.fefu.pokeabilityapp

import ru.fefu.pokeabilityapp.domain.model.AbilityItem
import ru.fefu.pokeabilityapp.domain.repository.FavouriteRepository

class FakeFavouriteRepository : FavouriteRepository {
    val favourites: MutableList<AbilityItem> = mutableListOf()
    var failGetAll = false

    override suspend fun getAll(): List<AbilityItem> {
        if (failGetAll) error("getAll failed")
        return favourites.toList()
    }

    override suspend fun add(item: AbilityItem) {
        favourites.removeAll { it.id == item.id }
        favourites.add(0, item)
    }

    override suspend fun remove(id: Int) {
        favourites.removeAll { it.id == id }
    }
}