package ru.fefu.pokeabilityapp.ui.list

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.launch
import ru.fefu.pokeabilityapp.domain.model.AbilityFilter
import ru.fefu.pokeabilityapp.domain.model.AbilityItem
import ru.fefu.pokeabilityapp.domain.repository.AbilityRepository
import ru.fefu.pokeabilityapp.domain.repository.FavouriteRepository
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class AbilityListViewModel @Inject constructor(
    private val repository: AbilityRepository,
    private val favouriteRepository: FavouriteRepository
) : ViewModel() {

    private var currentOffset = 0

    var uiState by mutableStateOf(AbilityListUiState())
        private set

    val visibleAbilities: List<AbilityItem>
        get() = when (uiState.filter) {
            AbilityFilter.ALL -> uiState.items
            AbilityFilter.FAVOURITES -> uiState.items.filter { it.id in uiState.favourites }
        }

    init {
        loadAbilities()
        loadFavourites()
    }


    fun loadAbilities() {
        currentOffset = 0
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val result = repository.getAbilities(offset = 0)
                currentOffset = result.size
                uiState = uiState.copy(
                    isLoading = false,
                    items = result,
                    canLoadMore = result.size >= 20
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                uiState = uiState.copy(isLoading = false, errorMessage = "Network error")
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, errorMessage = "Unknown error")
            }
        }
    }

    fun loadMore() {
        if (uiState.isLoadingMore || !uiState.canLoadMore) return
        viewModelScope.launch {
            uiState = uiState.copy(isLoadingMore = true)
            try {
                val result = repository.getAbilities(offset = currentOffset)
                currentOffset += result.size
                uiState = uiState.copy(
                    isLoadingMore = false,
                    items = uiState.items + result,
                    canLoadMore = result.size >= 20
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                uiState = uiState.copy(isLoadingMore = false, errorMessage = "Network error")
            } catch (e: Exception) {
                uiState = uiState.copy(isLoadingMore = false, errorMessage = "Unknown error")
            }
        }
    }

    private fun loadFavourites() {
        viewModelScope.launch {
            try {
                val favs = favouriteRepository.getAll()
                uiState = uiState.copy(favourites = favs.map { it.id }.toSet())
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                uiState = uiState.copy(errorMessage = "Network error")
            } catch (e: Exception) {
                uiState = uiState.copy(errorMessage = "Database error")
            }
        }
    }

    fun onFilterChange(f: AbilityFilter) {
        uiState = uiState.copy(filter = f)
    }

    fun searchAndNavigate(query: String, onNavigate: (Int) -> Unit) {
        if (query.isBlank()) return
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, errorMessage = null)
            try {
                val result = repository.getAbilityByName(query)
                if (result == null) {
                    uiState = uiState.copy(isLoading = false, errorMessage = "Ability not found")
                } else {
                    uiState = uiState.copy(isLoading = false)
                    onNavigate(result.id)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, errorMessage = "Network error")
            }
        }
    }

    fun clearSearch() {
        uiState = uiState.copy(searchQuery = "")
        loadAbilities()
    }

    fun toggleFavourite(id: Int) {
        viewModelScope.launch {
            try {
                val currentIds = uiState.favourites
                if (id in currentIds) {
                    favouriteRepository.remove(id)
                    uiState = uiState.copy(favourites = currentIds - id)
                } else {
                    val item = uiState.items.firstOrNull { it.id == id }
                    if (item == null) {
                        uiState = uiState.copy(errorMessage = "Failed to add to favourites")
                        return@launch
                    }
                    favouriteRepository.add(item)
                    uiState = uiState.copy(favourites = currentIds + id)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.e("FAV", "toggleFavourite error", e)
                uiState = uiState.copy(errorMessage = "Failed to update favourites")
            }
        }
    }
}