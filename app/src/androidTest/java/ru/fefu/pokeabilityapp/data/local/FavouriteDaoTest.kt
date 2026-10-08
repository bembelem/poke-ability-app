package ru.fefu.pokeabilityapp.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import ru.fefu.pokeabilityapp.data.local.dao.FavouriteDao
import ru.fefu.pokeabilityapp.data.local.entity.FavouriteEntity
import ru.fefu.pokeabilityapp.data.local.entity.ProfileEntity

@RunWith(AndroidJUnit4::class)
class FavouriteDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: FavouriteDao
    private var ash = 0L
    private var gary = 0L

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.getFavouriteDao()
        runBlocking {
            ash = database.getProfileDao().insert(ProfileEntity(name = "Ash", createdAt = 1))
            gary = database.getProfileDao().insert(ProfileEntity(name = "Gary", createdAt = 2))
        }
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insert_andGetAll_returnsItemsOrderedByAddedAtDesc() = runTest {
        val old = FavouriteEntity(profileId = ash, id = 1, name = "overgrow", addedAt = 100)
        val new = FavouriteEntity(profileId = ash, id = 2, name = "blaze", addedAt = 200)

        dao.insert(old)
        dao.insert(new)
        val result = dao.getAll(ash)

        assertEquals(listOf(new, old), result)
    }

    @Test
    fun deleteById_removesItem() = runTest {
        dao.insert(FavouriteEntity(profileId = ash, id = 1, name = "overgrow", addedAt = 100))

        dao.deleteById(ash, 1)
        val result = dao.getAll(ash)

        assertEquals(emptyList<FavouriteEntity>(), result)
    }

    @Test
    fun insert_duplicate_replacesExisting() = runTest {
        dao.insert(FavouriteEntity(profileId = ash, id = 1, name = "overgrow", addedAt = 100))
        dao.insert(FavouriteEntity(profileId = ash, id = 1, name = "overgrow", addedAt = 200))

        val result = dao.getAll(ash)

        assertEquals(1, result.size)
        assertEquals(200, result[0].addedAt)
    }

    @Test
    fun favourites_areSeparatedByProfile() = runTest {
        dao.insert(FavouriteEntity(profileId = ash, id = 1, name = "overgrow", addedAt = 100))
        dao.insert(FavouriteEntity(profileId = gary, id = 2, name = "blaze", addedAt = 100))

        assertEquals(listOf("overgrow"), dao.getAll(ash).map { it.name })
        assertEquals(listOf("blaze"), dao.getAll(gary).map { it.name })
    }

    @Test
    fun sameAbility_canBeFavouriteInTwoProfiles() = runTest {
        dao.insert(FavouriteEntity(profileId = ash, id = 1, name = "overgrow", addedAt = 100))
        dao.insert(FavouriteEntity(profileId = gary, id = 1, name = "overgrow", addedAt = 100))

        assertEquals(1, dao.getAll(ash).size)
        assertEquals(1, dao.getAll(gary).size)
    }

    @Test
    fun deletingProfile_removesItsFavourites() = runTest {
        dao.insert(FavouriteEntity(profileId = ash, id = 1, name = "overgrow", addedAt = 100))
        dao.insert(FavouriteEntity(profileId = gary, id = 2, name = "blaze", addedAt = 100))

        database.getProfileDao().deleteById(ash)

        assertEquals(emptyList<FavouriteEntity>(), dao.getAll(ash))
        assertEquals(1, dao.getAll(gary).size)
    }
}
