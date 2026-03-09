package ru.fefu.pokeabilityapp.ui.common

sealed class UiState<out T> {
    object Loading : UiState<Nothing>()
    data class Content<T>(val data: T) : UiState<T>()
    data class Error(val message: String) : UiState<Nothing>()
}
