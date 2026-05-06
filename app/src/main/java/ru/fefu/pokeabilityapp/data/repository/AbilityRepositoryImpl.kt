package ru.fefu.pokeabilityapp.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import ru.fefu.pokeabilityapp.data.service.PokeApiService
import ru.fefu.pokeabilityapp.data.toAbilityDetail
import ru.fefu.pokeabilityapp.data.toAbilityItemOrNull
import ru.fefu.pokeabilityapp.domain.model.AbilityDetail
import ru.fefu.pokeabilityapp.domain.model.AbilityItem
import ru.fefu.pokeabilityapp.domain.repository.AbilityRepository
import javax.inject.Inject

class AbilityRepositoryImpl @Inject constructor(
    private val api: PokeApiService
): AbilityRepository {
    override suspend fun getAbilities(offset: Int): List<AbilityItem> = withContext(Dispatchers.IO) {
        api.getAbilities(limit = 20, offset = offset)
            .results.mapNotNull { it.toAbilityItemOrNull() }
    }

    override suspend fun getAbilityById(id: Int): AbilityDetail = withContext(Dispatchers.IO) {
        api.getAbilityById(id).toAbilityDetail()
    }

    override suspend fun getAbilityByName(name: String): AbilityItem? = withContext(Dispatchers.IO) {
        try {
            val dto = api.getAbilityByName(name.trim().lowercase())
            AbilityItem(id = dto.id, name = dto.name)
        } catch (e: Exception) {
            null
        }
    }
}
