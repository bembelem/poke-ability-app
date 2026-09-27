package ru.fefu.pokeabilityapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.fefu.pokeabilityapp.domain.model.Tag

@Entity(
    tableName = "tags",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("profileId"),
        Index(value = ["profileId", "name"], unique = true)
    ]
)
data class TagEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val name: String,
    val colorArgb: Int
)

fun TagEntity.toDomain(): Tag = Tag(id, name, colorArgb)

@Entity(
    tableName = "slot_tags",
    primaryKeys = ["slotId", "tagId"],
    foreignKeys = [
        ForeignKey(
            entity = TeamSlotEntity::class,
            parentColumns = ["id"],
            childColumns = ["slotId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tagId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("tagId")]
)
data class SlotTagCrossRef(
    val slotId: Long,
    val tagId: Long
)
