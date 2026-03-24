package ru.fefu.pokeabilityapp.data.repository

import ru.fefu.pokeabilityapp.data.local.FavouriteDao
import ru.fefu.pokeabilityapp.data.local.FavouriteEntity
import ru.fefu.pokeabilityapp.data.local.toDomain
import ru.fefu.pokeabilityapp.domain.model.AbilityItem
import ru.fefu.pokeabilityapp.domain.repository.FavouriteRepository
import javax.inject.Inject

class FavouriteRepositoryImpl @Inject constructor(
    private val dao: FavouriteDao
) : FavouriteRepository {

    override suspend fun getAll(): List<AbilityItem> =
        dao.getAll().map { it.toDomain() }

    override suspend fun add(item: AbilityItem) =
        dao.insert(FavouriteEntity(id = item.id, name = item.name))

    override suspend fun remove(id: Int) =
        dao.deleteById(id)
}