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
import ru.fefu.pokeabilityapp.domain.repository.FavouriteRepository
import javax.inject.Inject

@HiltViewModel
class AbilityListViewModel @Inject constructor(
    private val repository: AbilityRepository,
    private val favouriteRepository: FavouriteRepository
) : ViewModel() {

    var uiState by mutableStateOf(AbilityListUiState())
        private set

    private var items by mutableStateOf(emptyList<AbilityItem>())
    private var favouriteItems by mutableStateOf(emptyList<AbilityItem>())

    val visibleAbilities: List<AbilityItem>
        get() = when (uiState.filter) {
            AbilityFilter.ALL -> items
            AbilityFilter.FAVOURITES -> favouriteItems
        }

    init {
        loadAbilities()
        loadFavourites()
    }

    fun loadAbilities() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                items = repository.getAbilities()
                uiState = uiState.copy(isLoading = false)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    errorMessage = e.message ?: "Unknown error"
                )
            }
        }
    }

    private fun loadFavourites() {
        viewModelScope.launch {
            try {
                val favs = favouriteRepository.getAll()
                favouriteItems = favs
                uiState = uiState.copy(favourites = favs.map { it.id }.toSet())
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = "Не удалось загрузить избранное")
            }
        }
    }

    fun onFilterChange(f: AbilityFilter) {
        uiState = uiState.copy(filter = f)
    }

    fun toggleFavourite(id: Int) {
        viewModelScope.launch {
            val currentIds = uiState.favourites
            val currentItems = favouriteItems
            if (id in currentIds) {
                favouriteRepository.remove(id)
                favouriteItems = currentItems.filterNot { it.id == id }
                uiState = uiState.copy(favourites = currentIds - id)
            } else {
                val item = items.firstOrNull { it.id == id }
                if (item == null) {
                    uiState = uiState.copy(errorMessage = "Не удалось добавить в избранное")
                    return@launch
                }
                favouriteRepository.add(item)
                favouriteItems = listOf(item) + currentItems
                uiState = uiState.copy(favourites = currentIds + id)
            }
        }
    }
}