package ru.fefu.pokeabilityapp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.fefu.pokeabilityapp.domain.model.AppSettings
import ru.fefu.pokeabilityapp.domain.model.ThemeMode
import ru.fefu.pokeabilityapp.domain.repository.SettingsRepository
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val repository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<AppSettings> = repository.observeSettings()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = AppSettings()
        )

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { repository.setThemeMode(mode) }
    }

    fun setCacheTtlHours(hours: Int) {
        viewModelScope.launch { repository.setCacheTtlHours(hours) }
    }

    fun setHistoryEnabled(enabled: Boolean) {
        viewModelScope.launch { repository.setHistoryEnabled(enabled) }
    }

    fun setThreatThreshold(value: Int) {
        viewModelScope.launch { repository.setThreatThreshold(value) }
    }
}
