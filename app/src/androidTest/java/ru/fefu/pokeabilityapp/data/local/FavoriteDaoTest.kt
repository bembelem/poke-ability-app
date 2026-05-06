package ru.fefu.pokeabilityapp.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavouriteDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: FavouriteDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.getFavouriteDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insert_andGetAll_returnsItemsOrderedByAddedAtDesc() = runTest {
        val old = FavouriteEntity(id = 1, name = "overgrow", addedAt = 100)
        val new = FavouriteEntity(id = 2, name = "blaze", addedAt = 200)

        dao.insert(old)
        dao.insert(new)
        val result = dao.getAll()

        assertEquals(listOf(new, old), result)
    }

    @Test
    fun deleteById_removesItem() = runTest {
        val item = FavouriteEntity(id = 1, name = "overgrow", addedAt = 100)
        dao.insert(item)

        dao.deleteById(1)
        val result = dao.getAll()

        assertEquals(emptyList<FavouriteEntity>(), result)
    }

    @Test
    fun insert_duplicate_replacesExisting() = runTest {
        val item = FavouriteEntity(id = 1, name = "overgrow", addedAt = 100)
        val updated = FavouriteEntity(id = 1, name = "overgrow", addedAt = 200)

        dao.insert(item)
        dao.insert(updated)
        val result = dao.getAll()

        assertEquals(1, result.size)
        assertEquals(200, result[0].addedAt)
    }
}