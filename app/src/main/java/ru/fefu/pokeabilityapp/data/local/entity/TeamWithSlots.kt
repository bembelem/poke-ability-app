package ru.fefu.pokeabilityapp.data.local.entity

import androidx.room.Embedded
import androidx.room.Relation
import ru.fefu.pokeabilityapp.domain.model.Team

data class TeamWithSlots(
    @Embedded val team: TeamEntity,
    @Relation(parentColumn = "id", entityColumn = "teamId")
    val slots: List<TeamSlotEntity>
)

fun TeamWithSlots.toDomain(): Team = Team(
    id = team.id,
    name = team.name,
    slots = slots.sortedBy { it.position }.map { it.toDomain() }
)
