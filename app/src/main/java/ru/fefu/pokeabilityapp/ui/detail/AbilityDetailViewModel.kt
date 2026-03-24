package ru.fefu.pokeabilityapp.ui.detail

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.fefu.pokeabilityapp.domain.repository.AbilityRepository
import javax.inject.Inject


@HiltViewModel
class AbilityDetailViewModel @Inject constructor(
    private val repository: AbilityRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val abilityId: Int = checkNotNull(savedStateHandle["abilityId"])

    var uiState by mutableStateOf<AbilityDetailUiState>(AbilityDetailUiState.Loading)
        private set

    init {
        loadDetail()
    }

    fun loadDetail() {
        viewModelScope.launch {
            uiState = AbilityDetailUiState.Loading
            try {
                val detail = repository.getAbilityById(abilityId)
                uiState = AbilityDetailUiState.Content(detail)
            } catch (e: Exception) {
                uiState = AbilityDetailUiState.Error(e.message ?: "Unknown error")
            }
        }
    }
}
