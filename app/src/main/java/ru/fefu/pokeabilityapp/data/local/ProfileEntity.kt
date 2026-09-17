package ru.fefu.pokeabilityapp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.fefu.pokeabilityapp.domain.model.Profile

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

fun ProfileEntity.toDomain(): Profile = Profile(id, name)
