package ru.fefu.pokeabilityapp.data.local

import androidx.room.testing.MigrationTestHelper
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class MigrationTest {

    private val dbName = "migration-test"

    @get:Rule
    val helper = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java
    )

    @Test
    fun migration1To2_movesFavouritesToDefaultProfile() {
        val oldDb = helper.createDatabase(dbName, 1)
        oldDb.execSQL("INSERT INTO favourites (id, name, addedAt) VALUES (1, 'overgrow', 100)")
        oldDb.close()

        val db = helper.runMigrationsAndValidate(dbName, 2, true, MIGRATION_1_2)

        val defaultProfileId = db.query("SELECT id FROM profiles LIMIT 1").use { cursor ->
            assertTrue(cursor.moveToFirst())
            cursor.getLong(0)
        }
        db.query("SELECT name, profileId FROM favourites").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("overgrow", cursor.getString(0))
            assertEquals(defaultProfileId, cursor.getLong(1))
        }
    }

    @Test
    fun migration1To2_createsDefaultProfile() {
        helper.createDatabase(dbName, 1).close()

        val db = helper.runMigrationsAndValidate(dbName, 2, true, MIGRATION_1_2)

        db.query("SELECT COUNT(*) FROM profiles").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(1, cursor.getInt(0))
        }
    }

    @Test
    fun migration2To3_createsTeamTables() {
        helper.createDatabase(dbName, 2).close()

        val db = helper.runMigrationsAndValidate(dbName, 3, true, MIGRATION_2_3)

        db.query("SELECT COUNT(*) FROM teams").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
        db.query("SELECT COUNT(*) FROM team_slots").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
    }

    @Test
    fun fullChain1To3_keepsFavourites() {
        val oldDb = helper.createDatabase(dbName, 1)
        oldDb.execSQL("INSERT INTO favourites (id, name, addedAt) VALUES (1, 'overgrow', 100)")
        oldDb.close()

        val db = helper.runMigrationsAndValidate(dbName, 3, true, MIGRATION_1_2, MIGRATION_2_3)

        db.query("SELECT name FROM favourites").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("overgrow", cursor.getString(0))
        }
    }
}
