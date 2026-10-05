package ru.fefu.pokeabilityapp.ui.teams

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.fefu.pokeabilityapp.domain.model.PokemonItem
import ru.fefu.pokeabilityapp.domain.repository.PokemonRepository
import ru.fefu.pokeabilityapp.domain.repository.TeamRepository
import ru.fefu.pokeabilityapp.work.BackgroundWork
import javax.inject.Inject

data class PokemonPickerUiState(
    val items: List<PokemonItem> = emptyList(),
    val query: String = "",
    val isLoading: Boolean = false,
    val isLoadingMore: Boolean = false,
    val errorMessage: String? = null
)

@HiltViewModel
class PokemonPickerViewModel @Inject constructor(
    private val pokemonRepository: PokemonRepository,
    private val teamRepository: TeamRepository,
    private val backgroundWork: BackgroundWork,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val teamId: Long = checkNotNull(savedStateHandle["teamId"])
    private val position: Int = checkNotNull(savedStateHandle["position"])

    private val query = MutableStateFlow("")
    private val isSyncing = MutableStateFlow(true)
    private val isLoadingMore = MutableStateFlow(false)
    private val syncError = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            try {
                pokemonRepository.refreshFirstPage(force = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                syncError.value = "Network error"
            }
            isSyncing.value = false
        }
    }

    val uiState: StateFlow<PokemonPickerUiState> = combine(
        pokemonRepository.observePokemon(),
        query,
        isSyncing,
        isLoadingMore,
        syncError
    ) { all, text, syncing, loadingMore, error ->
        val needle = text.trim()
        val filtered = if (needle.isEmpty()) {
            all
        } else {
            all.filter { it.name.contains(needle, ignoreCase = true) }
        }
        PokemonPickerUiState(
            items = filtered,
            query = text,
            isLoading = syncing && all.isEmpty(),
            isLoadingMore = loadingMore,
            // кэш важнее: пока есть что показать, ошибка не закрывает экран
            errorMessage = error.takeIf { all.isEmpty() }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = PokemonPickerUiState(isLoading = true)
    )

    fun onQueryChange(value: String) {
        query.value = value
    }

    fun loadMore() {
        if (isLoadingMore.value) return
        viewModelScope.launch {
            isLoadingMore.value = true
            try {
                pokemonRepository.loadNextPage()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                syncError.value = "Network error"
            }
            isLoadingMore.value = false
        }
    }

    // если сети нет, предзагрузка дождётся её и догрузит типы и способности в фоне
    fun pick(item: PokemonItem, onDone: () -> Unit) {
        viewModelScope.launch {
            teamRepository.setSlot(teamId, position, item.id, item.name)
            backgroundWork.requestTeamPrefetch()
            onDone()
        }
    }
}
