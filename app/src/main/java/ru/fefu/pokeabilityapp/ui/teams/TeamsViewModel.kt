package ru.fefu.pokeabilityapp.ui.teams

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.fefu.pokeabilityapp.domain.model.Team
import ru.fefu.pokeabilityapp.domain.repository.TeamRepository
import javax.inject.Inject

@HiltViewModel
class TeamsViewModel @Inject constructor(
    private val repository: TeamRepository
) : ViewModel() {

    val teams: StateFlow<List<Team>> = repository.observeTeams()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = emptyList()
        )

    fun createTeam(name: String, onCreated: (Long) -> Unit) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            onCreated(repository.createTeam(trimmed))
        }
    }

    fun deleteTeam(id: Long) {
        viewModelScope.launch { repository.deleteTeam(id) }
    }
}
