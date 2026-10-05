package ru.fefu.pokeabilityapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        FavouriteEntity::class,
        ProfileEntity::class,
        TeamEntity::class,
        TeamSlotEntity::class,
        CachedAbilityEntity::class,
        HistoryEntryEntity::class,
        CachedPokemonEntity::class,
        PokemonAbilityEntity::class,
        TypeEffectivenessEntity::class
    ],
    version = 8
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun getFavouriteDao(): FavouriteDao
    abstract fun getProfileDao(): ProfileDao
    abstract fun getTeamDao(): TeamDao
    abstract fun getAbilityCacheDao(): AbilityCacheDao
    abstract fun getHistoryDao(): HistoryDao
    abstract fun getPokemonCacheDao(): PokemonCacheDao
}
