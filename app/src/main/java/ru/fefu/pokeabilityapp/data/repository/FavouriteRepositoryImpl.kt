package ru.fefu.pokeabilityapp.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import ru.fefu.pokeabilityapp.data.local.FavouriteDao
import ru.fefu.pokeabilityapp.data.local.FavouriteEntity
import ru.fefu.pokeabilityapp.domain.repository.FavouriteRepository
import javax.inject.Inject

class FavouriteRepositoryImpl @Inject constructor(
    private val dao: FavouriteDao
) : FavouriteRepository {

    override fun getAll(): Flow<Set<Int>> =
        dao.getAll().map { list -> list.map { it.id }.toSet() }

    override suspend fun add(id: Int) =
        dao.insert(FavouriteEntity(id))

    override suspend fun remove(id: Int) =
        dao.delete(FavouriteEntity(id))
}