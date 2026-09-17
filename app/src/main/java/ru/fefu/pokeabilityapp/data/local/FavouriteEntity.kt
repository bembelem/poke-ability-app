package ru.fefu.pokeabilityapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import ru.fefu.pokeabilityapp.domain.model.AbilityItem

@Entity(
    tableName = "favourites",
    primaryKeys = ["profileId", "id"],
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
data class FavouriteEntity(
    val profileId: Long,
    val id: Int,
    val name: String,
    val addedAt: Long = System.currentTimeMillis()
)

fun FavouriteEntity.toDomain(): AbilityItem =
    AbilityItem(id, name)
