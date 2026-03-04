package ru.fefu.pokeabilityapp.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import ru.fefu.pokeabilityapp.domain.model.AbilityDetail
import ru.fefu.pokeabilityapp.domain.repository.AbilityRepository
import ru.fefu.pokeabilityapp.ui.common.UiState
import javax.inject.Inject


@HiltViewModel
class AbilityDetailViewModel @Inject constructor(
    private val repository: AbilityRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val abilityId: Int = checkNotNull(savedStateHandle["abilityId"])

    private val _uiState = MutableStateFlow<UiState<AbilityDetail>>(UiState.Loading)
    val uiState: StateFlow<UiState<AbilityDetail>> = _uiState.asStateFlow()

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val detail = repository.getAbilityById(abilityId)
                _uiState.value = UiState.Content(detail)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}