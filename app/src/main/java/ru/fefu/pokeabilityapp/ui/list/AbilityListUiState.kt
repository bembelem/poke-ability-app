package ru.fefu.pokeabilityapp.ui.list

import ru.fefu.pokeabilityapp.domain.model.AbilityFilter

data class AbilityListUiState(
    val favourites: Set<Int> = emptySet(),
    val filter: AbilityFilter = AbilityFilter.ALL,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)