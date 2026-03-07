package ru.fefu.pokeabilityapp.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.fefu.pokeabilityapp.domain.model.AbilityItem
import ru.fefu.pokeabilityapp.domain.repository.AbilityRepository
import ru.fefu.pokeabilityapp.ui.common.UiState
import javax.inject.Inject

@HiltViewModel
class AbilityListViewModel @Inject constructor(
    private val repository: AbilityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState<List<AbilityItem>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<AbilityItem>>> = _uiState.asStateFlow()

    // Для B6 - храним избранные id
    private val _favourites = MutableStateFlow<Set<Int>>(emptySet())
    val favourites: StateFlow<Set<Int>> = _favourites.asStateFlow()

    init {
        loadAbilities()
    }
    fun loadAbilities() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val abilities = repository.getAbilities()
                _uiState.value = UiState.Content(abilities)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun toggleFavourite(id: Int) {
        _favourites.update { current ->
            if (id in current) current - id else current + id
        }
    }
}

