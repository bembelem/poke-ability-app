package ru.fefu.pokeabilityapp.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

// listOrder = позиция в постраничном списке, -1 у найденных поиском и ещё не попавших в список
const val NOT_IN_LIST = -1

@Dao
interface AbilityCacheDao {

    @Query("SELECT * FROM cached_abilities WHERE listOrder >= 0 ORDER BY listOrder")
    fun observeList(): Flow<List<CachedAbilityEntity>>

    @Query("SELECT * FROM cached_abilities WHERE id = :id")
    fun observeById(id: Int): Flow<CachedAbilityEntity?>

    @Query("SELECT * FROM cached_abilities WHERE id = :id")
    suspend fun getById(id: Int): CachedAbilityEntity?

    @Query("SELECT * FROM cached_abilities WHERE name = :name")
    suspend fun getByName(name: String): CachedAbilityEntity?

    @Query("SELECT COUNT(*) FROM cached_abilities WHERE listOrder >= 0")
    suspend fun listCount(): Int

    @Query("SELECT MIN(fetchedAt) FROM cached_abilities WHERE listOrder >= 0")
    suspend fun oldestListFetchedAt(): Long?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(entity: CachedAbilityEntity)

    @Update
    suspend fun update(entity: CachedAbilityEntity)

    @Query("DELETE FROM cached_abilities")
    suspend fun clear()

    // список отдаёт только id, имя и порядок, остальные поля существующих строк не трогаем
    @Transaction
    suspend fun saveListEntries(entries: List<CachedAbilityEntity>) {
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
    suspend fun save(entity: CachedAbilityEntity) {
        if (getById(entity.id) == null) insert(entity) else update(entity)
    }
}
