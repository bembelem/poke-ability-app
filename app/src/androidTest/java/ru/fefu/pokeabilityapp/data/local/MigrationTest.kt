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
    fun migration3To4_createsTagTables() {
        helper.createDatabase(dbName, 3).close()

        val db = helper.runMigrationsAndValidate(dbName, 4, true, MIGRATION_3_4)

        db.query("SELECT COUNT(*) FROM tags").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
        db.query("SELECT COUNT(*) FROM slot_tags").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
    }

    @Test
    fun migration4To5_createsAbilityCache() {
        helper.createDatabase(dbName, 4).close()

        val db = helper.runMigrationsAndValidate(dbName, 5, true, MIGRATION_4_5)

        db.query("SELECT COUNT(*) FROM cached_abilities").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
    }

    @Test
    fun migration5To6_createsHistoryTable() {
        helper.createDatabase(dbName, 5).close()

        val db = helper.runMigrationsAndValidate(dbName, 6, true, MIGRATION_5_6)

        db.query("SELECT COUNT(*) FROM history_entries").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals(0, cursor.getInt(0))
        }
    }

    @Test
    fun migration6To7_createsPokemonCache() {
        helper.createDatabase(dbName, 6).close()

        val db = helper.runMigrationsAndValidate(dbName, 7, true, MIGRATION_6_7)

        listOf("cached_pokemon", "pokemon_abilities", "type_effectiveness").forEach { table ->
            db.query("SELECT COUNT(*) FROM $table").use { cursor ->
                assertTrue(cursor.moveToFirst())
                assertEquals(0, cursor.getInt(0))
            }
        }
    }

    @Test
    fun migration7To8_keepsTeamsAndDropsTags() {
        val oldDb = helper.createDatabase(dbName, 7)
        oldDb.execSQL("INSERT INTO profiles (id, name, createdAt) VALUES (1, 'Ash', 1)")
        oldDb.execSQL(
            "INSERT INTO teams (id, profileId, name, note, createdAt, updatedAt) " +
                "VALUES (1, 1, 'main', 'note', 1, 1)"
        )
        oldDb.execSQL(
            "INSERT INTO team_slots " +
                "(id, teamId, position, pokemonId, pokemonName, abilityId, abilityName, nickname, note) " +
                "VALUES (1, 1, 0, 6, 'charizard', 66, 'blaze', 'Zard', 'lead')"
        )
        oldDb.execSQL("INSERT INTO tags (id, profileId, name, colorArgb) VALUES (1, 1, 'lead', 0)")
        oldDb.execSQL("INSERT INTO slot_tags (slotId, tagId) VALUES (1, 1)")
        oldDb.close()

        val db = helper.runMigrationsAndValidate(dbName, 8, true, MIGRATION_7_8)

        db.query("SELECT name FROM teams").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("main", cursor.getString(0))
        }
        db.query("SELECT pokemonName, abilityName FROM team_slots").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("charizard", cursor.getString(0))
            assertEquals("blaze", cursor.getString(1))
        }
        db.query("SELECT name FROM sqlite_master WHERE name IN ('tags', 'slot_tags')").use { cursor ->
            assertEquals(0, cursor.count)
        }
    }

    @Test
    fun fullChain1To8_keepsFavourites() {
        val oldDb = helper.createDatabase(dbName, 1)
        oldDb.execSQL("INSERT INTO favourites (id, name, addedAt) VALUES (1, 'overgrow', 100)")
        oldDb.close()

        val db = helper.runMigrationsAndValidate(
            dbName,
            8,
            true,
            MIGRATION_1_2,
            MIGRATION_2_3,
            MIGRATION_3_4,
            MIGRATION_4_5,
            MIGRATION_5_6,
            MIGRATION_6_7,
            MIGRATION_7_8
        )

        db.query("SELECT name FROM favourites").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("overgrow", cursor.getString(0))
        }
    }
}
