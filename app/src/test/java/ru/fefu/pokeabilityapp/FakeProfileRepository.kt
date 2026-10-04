package ru.fefu.pokeabilityapp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import ru.fefu.pokeabilityapp.domain.model.Profile
import ru.fefu.pokeabilityapp.domain.repository.ProfileRepository
import ru.fefu.pokeabilityapp.domain.repository.SettingsRepository

class FakeProfileRepository(
    private val settings: SettingsRepository,
    initial: List<Profile> = listOf(Profile(1, "Тренер"))
) : ProfileRepository {

    private val profiles = MutableStateFlow(initial)
    private var nextId = (initial.maxOfOrNull { it.id } ?: 0) + 1

    override fun observeProfiles(): Flow<List<Profile>> = profiles

    override suspend fun createProfile(name: String): Long {
        val id = nextId++
        profiles.update { it + Profile(id, name) }
        return id
    }

    override suspend fun renameProfile(id: Long, name: String) {
        profiles.update { list -> list.map { if (it.id == id) it.copy(name = name) else it } }
    }

    override suspend fun deleteProfile(id: Long) {
        profiles.update { list -> list.filterNot { it.id == id } }
    }

    override suspend fun ensureActiveProfile(): Long {
        val activeId = settings.observeSettings().first().activeProfileId
        val resolved = profiles.value.firstOrNull { it.id == activeId }?.id
            ?: profiles.value.first().id
        settings.setActiveProfile(resolved)
        return resolved
    }
}
