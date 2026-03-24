package ru.fefu.pokeabilityapp.ui.detail

import ru.fefu.pokeabilityapp.domain.model.AbilityDetail

sealed class AbilityDetailUiState {
    object Loading : AbilityDetailUiState()
    data class Content(val detail: AbilityDetail) : AbilityDetailUiState()
    data class Error(val message: String) : AbilityDetailUiState()
}