package ru.fefu.pokeabilityapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ProfileDao {
    @Query("SELECT * FROM profiles ORDER BY createdAt")
    fun observeAll(): Flow<List<ProfileEntity>>

    @Query("SELECT * FROM profiles ORDER BY createdAt")
    suspend fun getAll(): List<ProfileEntity>

    @Query("SELECT * FROM profiles WHERE id = :id")
    suspend fun getById(id: Long): ProfileEntity?

    @Insert
    suspend fun insert(entity: ProfileEntity): Long

    @Query("UPDATE profiles SET name = :name WHERE id = :id")
    suspend fun rename(id: Long, name: String)

    @Query("DELETE FROM profiles WHERE id = :id")
    suspend fun deleteById(id: Long)
}
