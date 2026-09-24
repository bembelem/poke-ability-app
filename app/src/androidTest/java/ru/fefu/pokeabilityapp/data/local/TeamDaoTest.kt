package ru.fefu.pokeabilityapp.data.local

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TeamDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: TeamDao
    private var ash = 0L
    private var gary = 0L

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.getTeamDao()
        runBlocking {
            ash = database.getProfileDao().insert(ProfileEntity(name = "Ash", createdAt = 1))
            gary = database.getProfileDao().insert(ProfileEntity(name = "Gary", createdAt = 2))
        }
    }

    @After
    fun closeDb() {
        database.close()
    }

    private suspend fun createTeam(profileId: Long, name: String): Long =
        dao.insertTeam(TeamEntity(profileId = profileId, name = name))

    private fun slot(teamId: Long, position: Int, pokemonId: Int, name: String) =
        TeamSlotEntity(
            teamId = teamId,
            position = position,
            pokemonId = pokemonId,
            pokemonName = name
        )

    @Test
    fun teamWithSlots_returnsSlotsOrderedByPosition() = runTest {
        val teamId = createTeam(ash, "main")
        dao.insertSlot(slot(teamId, position = 2, pokemonId = 25, name = "pikachu"))
        dao.insertSlot(slot(teamId, position = 0, pokemonId = 6, name = "charizard"))

        val team = dao.getTeams(ash).single().toDomain()

        assertEquals(listOf("charizard", "pikachu"), team.slots.map { it.pokemonName })
    }

    @Test
    fun teams_areSeparatedByProfile() = runTest {
        createTeam(ash, "ash team")
        createTeam(gary, "gary team")

        assertEquals(listOf("ash team"), dao.getTeams(ash).map { it.team.name })
        assertEquals(listOf("gary team"), dao.getTeams(gary).map { it.team.name })
    }

    @Test
    fun deletingTeam_removesItsSlots() = runTest {
        val teamId = createTeam(ash, "main")
        dao.insertSlot(slot(teamId, position = 0, pokemonId = 25, name = "pikachu"))

        dao.deleteTeam(teamId)

        assertNull(dao.getSlotAt(teamId, 0))
        assertEquals(emptyList<TeamWithSlots>(), dao.getTeams(ash))
    }

    @Test
    fun deletingProfile_removesItsTeams() = runTest {
        val teamId = createTeam(ash, "main")
        dao.insertSlot(slot(teamId, position = 0, pokemonId = 25, name = "pikachu"))

        database.getProfileDao().deleteById(ash)

        assertEquals(emptyList<TeamWithSlots>(), dao.getTeams(ash))
        assertNull(dao.getSlotAt(teamId, 0))
    }

    @Test(expected = SQLiteConstraintException::class)
    fun twoSlotsOnSamePosition_areRejected() = runTest {
        val teamId = createTeam(ash, "main")
        dao.insertSlot(slot(teamId, position = 0, pokemonId = 25, name = "pikachu"))

        dao.insertSlot(slot(teamId, position = 0, pokemonId = 6, name = "charizard"))
    }

    @Test
    fun updateSlot_keepsSlotId() = runTest {
        val teamId = createTeam(ash, "main")
        val slotId = dao.insertSlot(slot(teamId, position = 0, pokemonId = 25, name = "pikachu"))

        val stored = dao.getSlot(slotId)!!
        dao.updateSlot(stored.copy(abilityId = 9, abilityName = "static"))

        val updated = dao.getSlot(slotId)
        assertEquals(slotId, updated?.id)
        assertEquals("static", updated?.abilityName)
    }

    @Test
    fun deleteSlotAt_removesOnlyThatPosition() = runTest {
        val teamId = createTeam(ash, "main")
        dao.insertSlot(slot(teamId, position = 0, pokemonId = 25, name = "pikachu"))
        dao.insertSlot(slot(teamId, position = 1, pokemonId = 6, name = "charizard"))

        dao.deleteSlotAt(teamId, 0)

        val names = dao.getTeams(ash).single().toDomain().slots.map { it.pokemonName }
        assertEquals(listOf("charizard"), names)
    }
}
