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
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TagDaoTest {

    private lateinit var database: AppDatabase
    private lateinit var dao: TagDao
    private var ash = 0L
    private var gary = 0L
    private var teamId = 0L
    private var slotA = 0L
    private var slotB = 0L

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = database.getTagDao()
        runBlocking {
            ash = database.getProfileDao().insert(ProfileEntity(name = "Ash", createdAt = 1))
            gary = database.getProfileDao().insert(ProfileEntity(name = "Gary", createdAt = 2))

            val teamDao = database.getTeamDao()
            teamId = teamDao.insertTeam(TeamEntity(profileId = ash, name = "main"))
            slotA = teamDao.insertSlot(
                TeamSlotEntity(teamId = teamId, position = 0, pokemonId = 25, pokemonName = "pikachu")
            )
            slotB = teamDao.insertSlot(
                TeamSlotEntity(teamId = teamId, position = 1, pokemonId = 6, pokemonName = "charizard")
            )
        }
    }

    @After
    fun closeDb() {
        database.close()
    }

    private suspend fun createTag(profileId: Long, name: String): Long =
        dao.insert(TagEntity(profileId = profileId, name = name, colorArgb = 0xFF00FF00.toInt()))

    @Test
    fun slot_canHaveSeveralTags() = runTest {
        val lead = createTag(ash, "lead")
        val wall = createTag(ash, "wall")

        dao.attach(SlotTagCrossRef(slotA, lead))
        dao.attach(SlotTagCrossRef(slotA, wall))

        assertEquals(listOf("lead", "wall"), dao.getTagsForSlot(slotA).map { it.name })
    }

    @Test
    fun tag_canBeOnSeveralSlots() = runTest {
        val lead = createTag(ash, "lead")

        dao.attach(SlotTagCrossRef(slotA, lead))
        dao.attach(SlotTagCrossRef(slotB, lead))

        assertEquals(listOf("lead"), dao.getTagsForSlot(slotA).map { it.name })
        assertEquals(listOf("lead"), dao.getTagsForSlot(slotB).map { it.name })
    }

    @Test
    fun attachingSameTagTwice_isIgnored() = runTest {
        val lead = createTag(ash, "lead")

        dao.attach(SlotTagCrossRef(slotA, lead))
        dao.attach(SlotTagCrossRef(slotA, lead))

        assertEquals(1, dao.getTagsForSlot(slotA).size)
    }

    @Test
    fun detach_removesOnlyThatLink() = runTest {
        val lead = createTag(ash, "lead")
        val wall = createTag(ash, "wall")
        dao.attach(SlotTagCrossRef(slotA, lead))
        dao.attach(SlotTagCrossRef(slotA, wall))

        dao.detach(slotA, lead)

        assertEquals(listOf("wall"), dao.getTagsForSlot(slotA).map { it.name })
    }

    @Test
    fun deletingTag_removesLinksButKeepsSlot() = runTest {
        val lead = createTag(ash, "lead")
        dao.attach(SlotTagCrossRef(slotA, lead))

        dao.deleteTag(lead)

        assertEquals(emptyList<SlotTagRow>(), dao.getTagsForSlot(slotA))
        assertEquals(slotA, database.getTeamDao().getSlot(slotA)?.id)
    }

    @Test
    fun deletingSlot_removesLinksButKeepsTag() = runTest {
        val lead = createTag(ash, "lead")
        dao.attach(SlotTagCrossRef(slotA, lead))

        database.getTeamDao().deleteSlotAt(teamId, position = 0)

        assertEquals(emptyList<SlotTagRow>(), dao.getTagsForSlot(slotA))
        assertEquals(listOf("lead"), dao.getTags(ash).map { it.name })
    }

    @Test
    fun tags_areSeparatedByProfile() = runTest {
        createTag(ash, "lead")
        createTag(gary, "wall")

        assertEquals(listOf("lead"), dao.getTags(ash).map { it.name })
        assertEquals(listOf("wall"), dao.getTags(gary).map { it.name })
    }

    @Test
    fun sameTagName_isAllowedInDifferentProfiles() = runTest {
        createTag(ash, "lead")
        createTag(gary, "lead")

        assertEquals(1, dao.getTags(ash).size)
        assertEquals(1, dao.getTags(gary).size)
    }

    @Test(expected = SQLiteConstraintException::class)
    fun duplicateTagName_inSameProfile_isRejected() = runTest {
        createTag(ash, "lead")

        createTag(ash, "lead")
    }

    @Test
    fun observeTagsForSlots_groupsBySlot() = runTest {
        val lead = createTag(ash, "lead")
        val wall = createTag(ash, "wall")
        dao.attach(SlotTagCrossRef(slotA, lead))
        dao.attach(SlotTagCrossRef(slotB, wall))

        val rows = dao.getTagsForSlot(slotA) + dao.getTagsForSlot(slotB)

        assertEquals(listOf(slotA, slotB), rows.map { it.slotId })
        assertEquals(listOf("lead", "wall"), rows.map { it.name })
    }
}
