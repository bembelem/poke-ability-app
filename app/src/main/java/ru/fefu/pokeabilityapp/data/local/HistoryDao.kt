package ru.fefu.pokeabilityapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface HistoryDao {

    @Query("SELECT * FROM history_entries WHERE profileId = :profileId ORDER BY viewedAt DESC")
    fun observeAll(profileId: Long): Flow<List<HistoryEntryEntity>>

    @Query("SELECT * FROM history_entries WHERE profileId = :profileId ORDER BY viewedAt DESC")
    suspend fun getAll(profileId: Long): List<HistoryEntryEntity>

    @Insert
    suspend fun insert(entry: HistoryEntryEntity)

    @Query("DELETE FROM history_entries WHERE profileId = :profileId AND abilityId = :abilityId")
    suspend fun deleteFor(profileId: Long, abilityId: Int)

    @Query("DELETE FROM history_entries WHERE profileId = :profileId")
    suspend fun clear(profileId: Long)
}
