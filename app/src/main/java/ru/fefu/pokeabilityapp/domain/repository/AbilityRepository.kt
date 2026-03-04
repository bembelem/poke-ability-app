package ru.fefu.pokeabilityapp.domain.repository

import ru.fefu.pokeabilityapp.domain.model.AbilityDetail
import ru.fefu.pokeabilityapp.domain.model.AbilityItem

// Контракт получения данных
interface AbilityRepository {
    suspend fun getAbilities(offset: Int = 0): List<AbilityItem>
    suspend fun getAbilityById(id: Int): AbilityDetail
}