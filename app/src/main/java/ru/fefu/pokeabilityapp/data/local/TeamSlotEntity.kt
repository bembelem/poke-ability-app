package ru.fefu.pokeabilityapp.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import ru.fefu.pokeabilityapp.domain.model.TeamSlot

@Entity(
    tableName = "team_slots",
    foreignKeys = [
        ForeignKey(
            entity = TeamEntity::class,
            parentColumns = ["id"],
            childColumns = ["teamId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index("teamId"),
        Index(value = ["teamId", "position"], unique = true)
    ]
)
data class TeamSlotEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val teamId: Long,
    val position: Int,
    val pokemonId: Int,
    val pokemonName: String,
    val abilityId: Int? = null,
    val abilityName: String? = null,
    val nickname: String? = null,
    val note: String? = null
)

fun TeamSlotEntity.toDomain(): TeamSlot = TeamSlot(
    id = id,
    position = position,
    pokemonId = pokemonId,
    pokemonName = pokemonName,
    abilityId = abilityId,
    abilityName = abilityName,
    nickname = nickname,
    note = note
)
