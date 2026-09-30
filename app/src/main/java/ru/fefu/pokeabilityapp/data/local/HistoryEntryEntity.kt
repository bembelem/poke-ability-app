package ru.fefu.pokeabilityapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.fefu.pokeabilityapp.domain.model.HistoryEntry

@Entity(
    tableName = "history_entries",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profileId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("profileId")]
)
data class HistoryEntryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val abilityId: Int,
    val abilityName: String,
    val viewedAt: Long
)

fun HistoryEntryEntity.toDomain(): HistoryEntry =
    HistoryEntry(abilityId = abilityId, abilityName = abilityName, viewedAt = viewedAt)
