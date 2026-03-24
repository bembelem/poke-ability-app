package ru.fefu.pokeabilityapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavouriteEntity)

    @Query("DELETE FROM favourites WHERE id = :id")
    suspend fun deleteById(id: Int)

    @Query("SELECT * FROM favourites ORDER BY addedAt DESC")
    suspend fun getAll(): List<FavouriteEntity>
}