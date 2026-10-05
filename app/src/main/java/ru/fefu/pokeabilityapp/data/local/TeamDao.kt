package ru.fefu.pokeabilityapp.data.local

import androidx.room.Dao
import androidx.room.Embedded
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
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

@Dao
interface TeamDao {

    @Transaction
    @Query("SELECT * FROM teams WHERE profileId = :profileId ORDER BY updatedAt DESC")
    fun observeTeams(profileId: Long): Flow<List<TeamWithSlots>>

    @Transaction
    @Query("SELECT * FROM teams WHERE id = :teamId")
    fun observeTeam(teamId: Long): Flow<TeamWithSlots?>

    @Insert
    suspend fun insertTeam(team: TeamEntity): Long

    @Query("UPDATE teams SET updatedAt = :updatedAt WHERE id = :id")
    suspend fun touchTeam(id: Long, updatedAt: Long)

    @Query("DELETE FROM teams WHERE id = :id")
    suspend fun deleteTeam(id: Long)

    @Query("SELECT * FROM team_slots WHERE teamId = :teamId AND position = :position")
    suspend fun getSlotAt(teamId: Long, position: Int): TeamSlotEntity?

    @Query("SELECT * FROM team_slots WHERE id = :slotId")
    suspend fun getSlot(slotId: Long): TeamSlotEntity?

    @Insert
    suspend fun insertSlot(slot: TeamSlotEntity): Long

    @Update
    suspend fun updateSlot(slot: TeamSlotEntity)

    @Query("DELETE FROM team_slots WHERE teamId = :teamId AND position = :position")
    suspend fun deleteSlotAt(teamId: Long, position: Int)
}
