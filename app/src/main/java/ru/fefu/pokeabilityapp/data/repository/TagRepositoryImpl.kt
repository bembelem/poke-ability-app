package ru.fefu.pokeabilityapp.data.repository

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import ru.fefu.pokeabilityapp.data.local.SlotTagCrossRef
import ru.fefu.pokeabilityapp.data.local.TagDao
import ru.fefu.pokeabilityapp.data.local.TagEntity
import ru.fefu.pokeabilityapp.data.local.toDomain
import ru.fefu.pokeabilityapp.domain.model.Tag
import ru.fefu.pokeabilityapp.domain.repository.ProfileRepository
import ru.fefu.pokeabilityapp.domain.repository.SettingsRepository
import ru.fefu.pokeabilityapp.domain.repository.TagRepository
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class TagRepositoryImpl @Inject constructor(
    private val dao: TagDao,
    private val settingsRepository: SettingsRepository,
    private val profileRepository: ProfileRepository
) : TagRepository {

    override fun observeTags(): Flow<List<Tag>> =
        settingsRepository.observeSettings()
            .map { it.activeProfileId }
            .distinctUntilChanged()
            .flatMapLatest { profileId ->
                dao.observeTags(profileId).map { list -> list.map { it.toDomain() } }
            }

    override fun observeTagsForSlots(slotIds: List<Long>): Flow<Map<Long, List<Tag>>> =
        dao.observeTagsForSlots(slotIds).map { rows ->
            rows.groupBy({ it.slotId }, { Tag(it.tagId, it.name, it.colorArgb) })
        }

    override suspend fun createTag(name: String, colorArgb: Int): Long =
        withContext(Dispatchers.IO) {
            val profileId = profileRepository.ensureActiveProfile()
            dao.findByName(profileId, name)?.id
                ?: dao.insert(TagEntity(profileId = profileId, name = name, colorArgb = colorArgb))
        }

    override suspend fun deleteTag(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteTag(id)
    }

    override suspend fun attachTag(slotId: Long, tagId: Long) = withContext(Dispatchers.IO) {
        dao.attach(SlotTagCrossRef(slotId = slotId, tagId = tagId))
    }

    override suspend fun detachTag(slotId: Long, tagId: Long) = withContext(Dispatchers.IO) {
        dao.detach(slotId, tagId)
    }
}
