package ru.fefu.pokeabilityapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface PokemonCacheDao {

    @Query("SELECT * FROM cached_pokemon WHERE listOrder >= 0 ORDER BY listOrder")
    fun observeList(): Flow<List<CachedPokemonEntity>>

    @Query("SELECT * FROM cached_pokemon WHERE id IN (:ids)")
    fun observeByIds(ids: List<Int>): Flow<List<CachedPokemonEntity>>

    @Query("SELECT * FROM cached_pokemon WHERE id = :id")
    suspend fun getById(id: Int): CachedPokemonEntity?

    @Query("SELECT * FROM cached_pokemon WHERE name = :name")
    suspend fun getByName(name: String): CachedPokemonEntity?

    @Query("SELECT COUNT(*) FROM cached_pokemon WHERE listOrder >= 0")
    suspend fun listCount(): Int

    @Query("SELECT MIN(fetchedAt) FROM cached_pokemon WHERE listOrder >= 0")
    suspend fun oldestListFetchedAt(): Long?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: CachedPokemonEntity)

    @Update
    suspend fun update(entity: CachedPokemonEntity)

    @Transaction
    suspend fun saveListEntries(entries: List<CachedPokemonEntity>) {
        entries.forEach { entry ->
            val existing = getById(entry.id)
            if (existing == null) {
                insert(entry)
            } else {
                update(
                    existing.copy(
                        name = entry.name,
                        listOrder = entry.listOrder,
                        fetchedAt = entry.fetchedAt
                    )
                )
            }
        }
    }

    @Transaction
    suspend fun save(entity: CachedPokemonEntity) {
        if (getById(entity.id) == null) insert(entity) else update(entity)
    }

    @Query("SELECT * FROM pokemon_abilities WHERE pokemonId IN (:pokemonIds) ORDER BY slot")
    fun observeAbilitiesFor(pokemonIds: List<Int>): Flow<List<PokemonAbilityEntity>>

    @Query("DELETE FROM pokemon_abilities WHERE pokemonId = :pokemonId")
    suspend fun deleteAbilitiesFor(pokemonId: Int)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAbilities(entries: List<PokemonAbilityEntity>)

    @Query("SELECT * FROM type_effectiveness")
    fun observeTypeChart(): Flow<List<TypeEffectivenessEntity>>

    @Query("SELECT MIN(fetchedAt) FROM type_effectiveness")
    suspend fun oldestTypeFetchedAt(): Long?

    @Query("SELECT COUNT(*) FROM type_effectiveness")
    suspend fun typeRelationCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTypeRelations(entries: List<TypeEffectivenessEntity>)
}
