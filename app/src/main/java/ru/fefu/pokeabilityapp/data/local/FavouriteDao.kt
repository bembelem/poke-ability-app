package ru.fefu.pokeabilityapp.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouriteDao{
    @Query("SELECT * FROM favourites")
    fun getAll(): Flow<List<FavouriteEntity>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(entity: FavouriteEntity)

    @Delete
    suspend fun delete(entity: FavouriteEntity)
}