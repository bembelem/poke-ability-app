package ru.fefu.pokeabilityapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [FavouriteEntity::class, ProfileEntity::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getFavouriteDao(): FavouriteDao
    abstract fun getProfileDao(): ProfileDao
}
