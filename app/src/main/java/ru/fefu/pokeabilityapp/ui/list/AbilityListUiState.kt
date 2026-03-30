package ru.fefu.pokeabilityapp.ui.list

import ru.fefu.pokeabilityapp.domain.model.AbilityFilter
import ru.fefu.pokeabilityapp.domain.model.AbilityItem

data class AbilityListUiState(
    val items: List<AbilityItem> = emptyList(),
    val favourites: Set<Int> = emptySet(),
    val filter: AbilityFilter = AbilityFilter.ALL,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)