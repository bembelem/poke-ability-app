package ru.fefu.pokeabilityapp.domain.repository

import kotlinx.coroutines.flow.Flow
import ru.fefu.pokeabilityapp.domain.model.Tag

interface TagRepository {
    fun observeTags(): Flow<List<Tag>>
    fun observeTagsForSlots(slotIds: List<Long>): Flow<Map<Long, List<Tag>>>

    suspend fun createTag(name: String, colorArgb: Int): Long
    suspend fun deleteTag(id: Long)
    suspend fun attachTag(slotId: Long, tagId: Long)
    suspend fun detachTag(slotId: Long, tagId: Long)
}
