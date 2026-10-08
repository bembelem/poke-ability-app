package ru.fefu.pokeabilityapp.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.fefu.pokeabilityapp.domain.model.Profile

interface ProfileRepository {
    fun observeProfiles(): Flow<List<Profile>>
    suspend fun createProfile(name: String): Long
    suspend fun deleteProfile(id: Long)

    /** Возвращает id активного профиля, создавая его при первом запуске. */
    suspend fun ensureActiveProfile(): Long
}
