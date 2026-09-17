package ru.fefu.pokeabilityapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavouriteEntity)

    @Query("DELETE FROM favourites WHERE profileId = :profileId AND id = :id")
    suspend fun deleteById(profileId: Long, id: Int)

    @Query("SELECT * FROM favourites WHERE profileId = :profileId ORDER BY addedAt DESC")
    suspend fun getAll(profileId: Long): List<FavouriteEntity>

    @Query("SELECT * FROM favourites WHERE profileId = :profileId ORDER BY addedAt DESC")
    fun observeAll(profileId: Long): Flow<List<FavouriteEntity>>
}
