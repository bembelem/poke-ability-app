package ru.fefu.pokeabilityapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavouriteEntity::class,
        ProfileEntity::class,
        TeamEntity::class,
        TeamSlotEntity::class,
        TagEntity::class,
        SlotTagCrossRef::class
    ],
    version = 4
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getFavouriteDao(): FavouriteDao
    abstract fun getProfileDao(): ProfileDao
    abstract fun getTeamDao(): TeamDao
    abstract fun getTagDao(): TagDao
}
