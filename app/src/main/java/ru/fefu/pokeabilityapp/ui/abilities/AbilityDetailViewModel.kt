package ru.fefu.pokeabilityapp.ui.abilities

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.fefu.pokeabilityapp.domain.repository.AbilityRepository
import ru.fefu.pokeabilityapp.domain.repository.HistoryRepository
import javax.inject.Inject

@HiltViewModel
class AbilityDetailViewModel @Inject constructor(
    private val repository: AbilityRepository,
    private val historyRepository: HistoryRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val abilityId: Int = checkNotNull(savedStateHandle["abilityId"])
    private val error = MutableStateFlow<String?>(null)

    val uiState: StateFlow<AbilityDetailUiState> =
        combine(repository.observeAbilityDetail(abilityId), error) { detail, message ->
            when {
                // кэш важнее ошибки сети: есть что показать, показываем
                detail != null -> AbilityDetailUiState.Content(detail)
                message != null -> AbilityDetailUiState.Error(message)
                else -> AbilityDetailUiState.Loading
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AbilityDetailUiState.Loading
        )

    init {
        loadDetail()
        viewModelScope.launch {
            val detail = repository.observeAbilityDetail(abilityId).filterNotNull().first()
            historyRepository.record(detail.id, detail.name)
        }
    }

    fun loadDetail() {
        viewModelScope.launch {
            error.value = null
            try {
                repository.refreshDetail(abilityId, force = false)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                error.value = "Network error"
            }
        }
    }
}
