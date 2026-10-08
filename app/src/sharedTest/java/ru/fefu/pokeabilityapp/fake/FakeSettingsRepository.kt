package ru.fefu.pokeabilityapp.fake

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import ru.fefu.pokeabilityapp.domain.model.AppSettings
import ru.fefu.pokeabilityapp.domain.model.ThemeMode
import ru.fefu.pokeabilityapp.domain.repository.SettingsRepository

class FakeSettingsRepository(initial: AppSettings = AppSettings()) : SettingsRepository {

    private val state = MutableStateFlow(initial)

    override fun observeSettings(): Flow<AppSettings> = state.asStateFlow()

    // в фейке у всех профилей одни настройки
    override suspend fun settingsFor(profileId: Long): AppSettings = state.value

    override suspend fun setActiveProfile(id: Long) {
        state.value = state.value.copy(activeProfileId = id)
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        state.value = state.value.copy(themeMode = mode)
    }

    override suspend fun setCacheTtlHours(hours: Int) {
        state.value = state.value.copy(cacheTtlHours = hours)
    }

    override suspend fun setAutoRefreshEnabled(enabled: Boolean) {
        state.value = state.value.copy(autoRefreshEnabled = enabled)
    }

    override suspend fun setRefreshOnWifiOnly(enabled: Boolean) {
        state.value = state.value.copy(refreshOnWifiOnly = enabled)
    }

    override suspend fun setHistoryEnabled(enabled: Boolean) {
        state.value = state.value.copy(historyEnabled = enabled)
    }

    override suspend fun setHistoryRetentionDays(days: Int) {
        state.value = state.value.copy(historyRetentionDays = days)
    }
}
