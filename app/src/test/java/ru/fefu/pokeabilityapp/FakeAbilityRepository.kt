package ru.fefu.pokeabilityapp

import ru.fefu.pokeabilityapp.domain.model.AbilityDetail
import ru.fefu.pokeabilityapp.domain.model.AbilityItem
import ru.fefu.pokeabilityapp.domain.repository.AbilityRepository

class FakeAbilityRepository : AbilityRepository {
    var abilities: List<AbilityItem> = emptyList()
    var detail: AbilityDetail? = null
    var failGetAbilities = false
    var failGetDetail = false

    override suspend fun getAbilities(offset: Int): List<AbilityItem> {
        if (failGetAbilities) error("getAbilities failed")
        return abilities
    }

    override suspend fun getAbilityById(id: Int): AbilityDetail {
        if (failGetDetail) error("getAbilityById failed")
        return detail ?: error("no detail set")
    }
}