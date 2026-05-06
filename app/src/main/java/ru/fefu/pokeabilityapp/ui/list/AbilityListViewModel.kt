package ru.fefu.pokeabilityapp.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.fefu.pokeabilityapp.domain.model.AbilityFilter
import ru.fefu.pokeabilityapp.domain.model.AbilityItem
import ru.fefu.pokeabilityapp.domain.repository.AbilityRepository
import ru.fefu.pokeabilityapp.domain.repository.FavouriteRepository
import javax.inject.Inject

private data class SearchState(
    val items: List<AbilityItem> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val hasSearched: Boolean = false,
)

private sealed interface SearchEvent {
    data object Reset : SearchEvent
    data object Loading : SearchEvent
    data class Success(val items: List<AbilityItem>) : SearchEvent
    data class Error(val message: String) : SearchEvent
}

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class AbilityListViewModel @Inject constructor(
    private val repository: AbilityRepository,
    private val favouriteRepository: FavouriteRepository
) : ViewModel() {

    private val queryFlow = MutableStateFlow("")
    private val filterFlow = MutableStateFlow(AbilityFilter.ALL)
    private val refreshRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    private data class PagingState(
        val items: List<AbilityItem> = emptyList(),
        val isLoading: Boolean = true,
        val isLoadingMore: Boolean = false,
        val canLoadMore: Boolean = true,
        val error: String? = null,
    )

    private val _pagingState = MutableStateFlow(PagingState())

    init {
        viewModelScope.launch {
            try {
                val result = repository.getAbilities(offset = 0)
                _pagingState.value = PagingState(
                    items = result,
                    isLoading = false,
                    canLoadMore = result.size >= 20
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _pagingState.value = PagingState(isLoading = false, error = "Network error")
            }
        }
    }

    fun loadMore() {
        val paging = _pagingState.value
        if (paging.isLoadingMore || !paging.canLoadMore) return
        viewModelScope.launch {
            _pagingState.value = paging.copy(isLoadingMore = true)
            try {
                val result = repository.getAbilities(offset = paging.items.size)
                _pagingState.value = paging.copy(
                    items = paging.items + result,
                    isLoadingMore = false,
                    canLoadMore = result.size >= 20
                )
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                _pagingState.value = paging.copy(isLoadingMore = false)
            }
        }
    }

    private val favouritesFlow: StateFlow<Set<Int>> =
        favouriteRepository.observeAll()
            .map { list -> list.map { it.id }.toSet() }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = emptySet()
            )

    private val searchQueryFlow =
        queryFlow
            .map { it.trim() }
            .debounce(400)
            .distinctUntilChanged()

    private val searchState: StateFlow<SearchState> =
        combine(
            searchQueryFlow,
            refreshRequests.onStart { emit(Unit) }
        ) { query, _ -> query }
            .flatMapLatest { query ->
                if (query.isBlank()) {
                    flowOf(SearchEvent.Reset)
                } else {
                    flow {
                        emit(SearchEvent.Loading)
                        val result = repository.getAbilityByName(query)
                        if (result == null) {
                            emit(SearchEvent.Success(emptyList()))
                        } else {
                            emit(SearchEvent.Success(listOf(result)))
                        }
                    }.catch {
                        emit(SearchEvent.Error("Network error"))
                    }
                }
            }
            .scan(SearchState()) { previous, event ->
                when (event) {
                    SearchEvent.Reset -> SearchState()
                    SearchEvent.Loading -> previous.copy(
                        isLoading = true,
                        errorMessage = null
                    )
                    is SearchEvent.Success -> SearchState(
                        items = event.items,
                        isLoading = false,
                        errorMessage = null,
                        hasSearched = true
                    )
                    is SearchEvent.Error -> previous.copy(
                        isLoading = false,
                        errorMessage = event.message,
                        hasSearched = true
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SearchState(isLoading = false)
            )

    val uiState: StateFlow<AbilityListUiState> =
        combine(
            queryFlow,
            filterFlow,
            favouritesFlow,
            searchState,
            _pagingState,
        ) { rawQuery, filter, favourites, search, paging ->
            val effectiveQuery = rawQuery.trim()
            val baseItems = if (effectiveQuery.isBlank()) paging.items else search.items
            val visibleItems = when (filter) {
                AbilityFilter.ALL -> baseItems
                AbilityFilter.FAVOURITES -> baseItems.filter { it.id in favourites }
            }
            AbilityListUiState(
                items = visibleItems,
                favourites = favourites,
                filter = filter,
                searchQuery = rawQuery,
                isLoading = if (effectiveQuery.isBlank()) paging.isLoading else search.isLoading,
                errorMessage = if (effectiveQuery.isBlank()) paging.error else search.errorMessage,
                hasSearched = search.hasSearched,
                canLoadMore = effectiveQuery.isBlank() && paging.canLoadMore,
                isLoadingMore = paging.isLoadingMore,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AbilityListUiState(isLoading = true)
        )

    fun onSearchQueryChange(query: String) {
        queryFlow.value = query
    }

    fun clearSearch() {
        queryFlow.value = ""
    }

    fun onFilterChange(filter: AbilityFilter) {
        filterFlow.value = filter
    }

    fun refresh() {
        refreshRequests.tryEmit(Unit)
    }

    fun toggleFavourite(id: Int) {
        viewModelScope.launch {
            try {
                val currentIds = favouritesFlow.value
                if (id in currentIds) {
                    favouriteRepository.remove(id)
                } else {
                    val item = _pagingState.value.items.firstOrNull { it.id == id }
                        ?: searchState.value.items.firstOrNull { it.id == id }
                        ?: return@launch
                    favouriteRepository.add(item)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) { }
        }
    }

    fun searchAndNavigate(query: String, onNavigate: (Int) -> Unit) {
        if (query.isBlank()) return
        viewModelScope.launch {
            try {
                val result = repository.getAbilityByName(query.trim().lowercase())
                if (result != null) onNavigate(result.id)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {

            }
        }
    }
}