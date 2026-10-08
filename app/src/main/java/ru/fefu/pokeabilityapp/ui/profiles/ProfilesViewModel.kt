package ru.fefu.pokeabilityapp.ui.profiles

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.fefu.pokeabilityapp.domain.repository.ProfileRepository
import ru.fefu.pokeabilityapp.domain.repository.SettingsRepository
import javax.inject.Inject

data class ProfileUi(
    val id: Long,
    val name: String,
    val isActive: Boolean
)

@HiltViewModel
class ProfilesViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val profiles: StateFlow<List<ProfileUi>> = combine(
        profileRepository.observeProfiles(),
        settingsRepository.observeSettings().map { it.activeProfileId }
    ) { profiles, activeId ->
        profiles.map { ProfileUi(it.id, it.name, it.id == activeId) }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = emptyList()
    )

    fun select(id: Long) {
        viewModelScope.launch { settingsRepository.setActiveProfile(id) }
    }

    fun create(name: String) {
        val trimmed = name.trim()
        if (trimmed.isEmpty()) return
        viewModelScope.launch {
            val id = profileRepository.createProfile(trimmed)
            settingsRepository.setActiveProfile(id)
        }
    }

    // последний профиль не удаляем - данным нужен владелец
    fun delete(id: Long) {
        viewModelScope.launch {
            if (profileRepository.observeProfiles().first().size <= 1) return@launch
            profileRepository.deleteProfile(id)
            profileRepository.ensureActiveProfile()
        }
    }
}
