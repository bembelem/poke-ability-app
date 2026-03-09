package ru.fefu.pokeabilityapp.ui.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.fefu.pokeabilityapp.domain.model.AbilityFilter
import ru.fefu.pokeabilityapp.domain.model.AbilityItem
import ru.fefu.pokeabilityapp.domain.repository.AbilityRepository
import ru.fefu.pokeabilityapp.ui.common.UiState
import javax.inject.Inject

@HiltViewModel
class AbilityListViewModel @Inject constructor(
    private val repository: AbilityRepository
) : ViewModel() {

    var uiState by mutableStateOf<UiState<List<AbilityItem>>>(UiState.Loading)
        private set

    private var items by mutableStateOf(emptyList<AbilityItem>())

    var favourites by mutableStateOf(emptySet<Int>())
        private set

    var filter by mutableStateOf(AbilityFilter.ALL)
        private set

    val visibleAbilities: List<AbilityItem>
        get() = when (filter) {
            AbilityFilter.ALL -> items
            AbilityFilter.FAVOURITES -> items.filter { it.id in favourites }
        }

    init { loadAbilities() }

    fun loadAbilities() {
        viewModelScope.launch {
            uiState = UiState.Loading
            try {
                items = repository.getAbilities()
                uiState = UiState.Content(items)
            } catch (e: Exception) {
                uiState = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun onFilterChange(f: AbilityFilter) { filter = f }

    fun toggleFavourite(id: Int) {
        favourites = if (id in favourites) favourites - id else favourites + id
    }
}