package ru.fefu.pokeabilityapp.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProfileDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: ProfileDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.getProfileDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insert_assignsIdAndReturnsProfile() = runTest {
        val id = dao.insert(ProfileEntity(name = "Ash", createdAt = 100))

        val stored = dao.getById(id)

        assertEquals("Ash", stored?.name)
    }

    @Test
    fun getAll_ordersByCreatedAt() = runTest {
        dao.insert(ProfileEntity(name = "second", createdAt = 200))
        dao.insert(ProfileEntity(name = "first", createdAt = 100))

        val result = dao.getAll().map { it.name }

        assertEquals(listOf("first", "second"), result)
    }

    @Test
    fun rename_updatesName() = runTest {
        val id = dao.insert(ProfileEntity(name = "Ash", createdAt = 100))

        dao.rename(id, "Gary")

        assertEquals("Gary", dao.getById(id)?.name)
    }

    @Test
    fun deleteById_removesProfile() = runTest {
        val id = dao.insert(ProfileEntity(name = "Ash", createdAt = 100))

        dao.deleteById(id)

        assertNull(dao.getById(id))
    }
}
