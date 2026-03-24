package ru.fefu.pokeabilityapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.fefu.pokeabilityapp.domain.model.AbilityItem

@Entity(tableName = "favourites")
data class FavouriteEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val addedAt: Long = System.currentTimeMillis()
)

fun FavouriteEntity.toDomain(): AbilityItem =
    AbilityItem(id, name)