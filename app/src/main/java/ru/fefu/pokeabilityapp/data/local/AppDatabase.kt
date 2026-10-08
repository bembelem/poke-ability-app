package ru.fefu.pokeabilityapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import ru.fefu.pokeabilityapp.data.local.dao.AbilityCacheDao
import ru.fefu.pokeabilityapp.data.local.dao.FavouriteDao
import ru.fefu.pokeabilityapp.data.local.dao.HistoryDao
import ru.fefu.pokeabilityapp.data.local.dao.PokemonCacheDao
import ru.fefu.pokeabilityapp.data.local.dao.ProfileDao
import ru.fefu.pokeabilityapp.data.local.dao.TeamDao
import ru.fefu.pokeabilityapp.data.local.entity.CachedAbilityEntity
import ru.fefu.pokeabilityapp.data.local.entity.CachedPokemonEntity
import ru.fefu.pokeabilityapp.data.local.entity.FavouriteEntity
import ru.fefu.pokeabilityapp.data.local.entity.HistoryEntryEntity
import ru.fefu.pokeabilityapp.data.local.entity.PokemonAbilityEntity
import ru.fefu.pokeabilityapp.data.local.entity.ProfileEntity
import ru.fefu.pokeabilityapp.data.local.entity.TeamEntity
import ru.fefu.pokeabilityapp.data.local.entity.TeamSlotEntity
import ru.fefu.pokeabilityapp.data.local.entity.TypeEffectivenessEntity

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
