package ru.fefu.pokeabilityapp.data.repository

import ru.fefu.pokeabilityapp.data.service.PokeApiService
import ru.fefu.pokeabilityapp.domain.model.AbilityDetail
import ru.fefu.pokeabilityapp.domain.model.AbilityItem
import javax.inject.Inject
import ru.fefu.pokeabilityapp.data.toAbilityItem
import ru.fefu.pokeabilityapp.data.toAbilityDetail
import ru.fefu.pokeabilityapp.domain.repository.AbilityRepository

class AbilityRepositoryImpl @Inject constructor(
    private val api: PokeApiService
): AbilityRepository {
    override suspend fun getAbilities(offset: Int): List<AbilityItem> {
        return api.getAbilities(limit = 20, offset = offset)
            .results
            .map { it.toAbilityItem() }
    }
    override suspend fun getAbilityById(id: Int): AbilityDetail {
        return api.getAbilityById(id).toAbilityDetail()
    }
}