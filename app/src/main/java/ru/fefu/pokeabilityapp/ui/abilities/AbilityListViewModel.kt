package ru.fefu.pokeabilityapp.ui.abilities

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
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
import kotlinx.coroutines.flow.scan
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import ru.fefu.pokeabilityapp.data.repository.ABILITY_PAGE_SIZE
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

private data class SyncState(
    val isRefreshing: Boolean = true,
    val isLoadingMore: Boolean = false,
    val canLoadMore: Boolean = true,
    val error: String? = null,
    val loadMoreError: String? = null,
)

private data class ListState(
    val items: List<AbilityItem>,
    val favourites: Set<Int>,
    val filter: AbilityFilter,
)

@OptIn(ExperimentalCoroutinesApi::class, FlowPreview::class)
@HiltViewModel
class AbilityListViewModel @Inject constructor(
    private val repository: AbilityRepository,
    private val favouriteRepository: FavouriteRepository
) : ViewModel() {

    private val queryFlow = MutableStateFlow("")
    private val filterFlow = MutableStateFlow(AbilityFilter.ALL)
    private val syncState = MutableStateFlow(SyncState())
    private val refreshRequests = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    init {
        syncFirstPage(force = false)
    }

    private val listState: StateFlow<ListState> =
        combine(
            repository.observeAbilities(),
            favouriteRepository.observeAll().map { list -> list.map { it.id }.toSet() },
            filterFlow
        ) { items, favourites, filter ->
            ListState(items, favourites, filter)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ListState(emptyList(), emptySet(), AbilityFilter.ALL)
        )

    private val searchState: StateFlow<SearchState> =
        combine(
            queryFlow.map { it.trim() }.debounce(400).distinctUntilChanged(),
            // счётчик попыток входит в ключ, поэтому повтор перезапускает поиск с тем же запросом
            refreshRequests.scan(0) { attempt, _ -> attempt + 1 }
        ) { query, attempt -> query to attempt }
            .distinctUntilChanged()
            .flatMapLatest { (query, _) ->
                if (query.isBlank()) {
                    flowOf(SearchEvent.Reset)
                } else {
                    flow {
                        emit(SearchEvent.Loading)
                        val result = repository.findByName(query)
                        emit(SearchEvent.Success(listOfNotNull(result)))
                    }.catch {
                        emit(SearchEvent.Error("Network error"))
                    }
                }
            }
            .scan(SearchState()) { previous, event ->
                when (event) {
                    SearchEvent.Reset -> SearchState()
                    SearchEvent.Loading -> previous.copy(isLoading = true, errorMessage = null)
                    is SearchEvent.Success -> SearchState(
                        items = event.items,
                        isLoading = false,
                        hasSearched = true
                    )
                    is SearchEvent.Error -> SearchState(
                        isLoading = false,
                        errorMessage = event.message,
                        hasSearched = true
                    )
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = SearchState()
            )

    val uiState: StateFlow<AbilityListUiState> =
        combine(
            queryFlow,
            listState,
            searchState,
            syncState
        ) { rawQuery, list, search, sync ->
            val searching = rawQuery.trim().isNotBlank()
            val baseItems = if (searching) search.items else list.items
            val visibleItems = when (list.filter) {
                AbilityFilter.ALL -> baseItems
                AbilityFilter.FAVOURITES -> baseItems.filter { it.id in list.favourites }
            }
            AbilityListUiState(
                items = visibleItems,
                favourites = list.favourites,
                filter = list.filter,
                searchQuery = rawQuery,
                isLoading = if (searching) search.isLoading else sync.isRefreshing && list.items.isEmpty(),
                // с данными в кэше сбой обновления не должен закрывать экран
                errorMessage = if (searching) {
                    search.errorMessage
                } else {
                    sync.error.takeIf { list.items.isEmpty() }
                },
                hasSearched = search.hasSearched,
                canLoadMore = !searching && sync.canLoadMore,
                isLoadingMore = sync.isLoadingMore,
                loadMoreError = if (searching) null else sync.loadMoreError,
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AbilityListUiState(isLoading = true)
        )

    private fun syncFirstPage(force: Boolean) {
        viewModelScope.launch {
            syncState.update { it.copy(isRefreshing = true, error = null) }
            try {
                repository.refreshFirstPage(force)
                syncState.update { it.copy(isRefreshing = false, canLoadMore = true) }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                syncState.update { it.copy(isRefreshing = false, error = "Network error") }
            }
        }
    }

    fun loadMore() {
        val current = syncState.value
        if (current.isLoadingMore || !current.canLoadMore) return
        viewModelScope.launch {
            syncState.update { it.copy(isLoadingMore = true, loadMoreError = null) }
            try {
                val loaded = repository.loadNextPage()
                syncState.update {
                    it.copy(isLoadingMore = false, canLoadMore = loaded >= ABILITY_PAGE_SIZE)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                syncState.update { it.copy(isLoadingMore = false, loadMoreError = "Network error") }
            }
        }
    }

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
        syncFirstPage(force = true)
    }

    fun toggleFavourite(id: Int) {
        viewModelScope.launch {
            try {
                if (id in listState.value.favourites) {
                    favouriteRepository.remove(id)
                } else {
                    val item = listState.value.items.firstOrNull { it.id == id }
                        ?: searchState.value.items.firstOrNull { it.id == id }
                        ?: return@launch
                    favouriteRepository.add(item)
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
            }
        }
    }
}
