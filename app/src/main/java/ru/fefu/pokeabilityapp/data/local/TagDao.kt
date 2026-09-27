package ru.fefu.pokeabilityapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

data class SlotTagRow(
    val slotId: Long,
    val tagId: Long,
    val name: String,
    val colorArgb: Int
)

@Dao
interface TagDao {

    @Query("SELECT * FROM tags WHERE profileId = :profileId ORDER BY name")
    fun observeTags(profileId: Long): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE profileId = :profileId ORDER BY name")
    suspend fun getTags(profileId: Long): List<TagEntity>

    @Query("SELECT * FROM tags WHERE profileId = :profileId AND name = :name")
    suspend fun findByName(profileId: Long, name: String): TagEntity?

    @Insert
    suspend fun insert(tag: TagEntity): Long

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteTag(id: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun attach(ref: SlotTagCrossRef)

    @Query("DELETE FROM slot_tags WHERE slotId = :slotId AND tagId = :tagId")
    suspend fun detach(slotId: Long, tagId: Long)

    @Query(
        "SELECT st.slotId AS slotId, t.id AS tagId, t.name AS name, t.colorArgb AS colorArgb " +
            "FROM slot_tags st INNER JOIN tags t ON t.id = st.tagId " +
            "WHERE st.slotId IN (:slotIds) ORDER BY t.name"
    )
    fun observeTagsForSlots(slotIds: List<Long>): Flow<List<SlotTagRow>>

    @Query(
        "SELECT st.slotId AS slotId, t.id AS tagId, t.name AS name, t.colorArgb AS colorArgb " +
            "FROM slot_tags st INNER JOIN tags t ON t.id = st.tagId " +
            "WHERE st.slotId = :slotId ORDER BY t.name"
    )
    suspend fun getTagsForSlot(slotId: Long): List<SlotTagRow>
}
