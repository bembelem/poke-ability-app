package ru.fefu.pokeabilityapp.domain.coverage

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import ru.fefu.pokeabilityapp.domain.model.PokeType
import ru.fefu.pokeabilityapp.domain.model.PokeType.ELECTRIC
import ru.fefu.pokeabilityapp.domain.model.PokeType.FIRE
import ru.fefu.pokeabilityapp.domain.model.PokeType.FLYING
import ru.fefu.pokeabilityapp.domain.model.PokeType.GRASS
import ru.fefu.pokeabilityapp.domain.model.PokeType.GROUND
import ru.fefu.pokeabilityapp.domain.model.PokeType.ROCK
import ru.fefu.pokeabilityapp.domain.model.PokeType.WATER
import ru.fefu.pokeabilityapp.domain.model.TypeChart

class CoverageAnalyzerTest {

    private val delta = 0.0001

    private fun chartOf(vararg relations: Triple<PokeType, PokeType, Double>): TypeChart =
        TypeChart(
            relations.associate { (attacking, defending, multiplier) ->
                (attacking to defending) to multiplier
            }
        )

    @Test
    fun `single type takes multiplier from chart`() {
        val chart = chartOf(Triple(GROUND, ELECTRIC, 2.0))

        val result = defensiveMultiplier(GROUND, listOf(ELECTRIC), null, chart)

        assertEquals(2.0, result, delta)
    }

    @Test
    fun `dual type multiplies both relations`() {
        val chart = chartOf(
            Triple(GROUND, FIRE, 2.0),
            Triple(GROUND, ROCK, 2.0),
        )

        val result = defensiveMultiplier(GROUND, listOf(FIRE, ROCK), null, chart)

        assertEquals(4.0, result, delta)
    }

    @Test
    fun `dual type weakness and resistance cancel out`() {
        val chart = chartOf(
            Triple(GROUND, FIRE, 2.0),
            Triple(GROUND, GRASS, 0.5),
        )

        val result = defensiveMultiplier(GROUND, listOf(FIRE, GRASS), null, chart)

        assertEquals(1.0, result, delta)
    }

    @Test
    fun `type immunity from chart gives zero`() {
        val chart = chartOf(Triple(GROUND, FLYING, 0.0))

        val result = defensiveMultiplier(GROUND, listOf(FLYING), null, chart)

        assertEquals(0.0, result, delta)
    }

    @Test
    fun `missing relation is neutral`() {
        val result = defensiveMultiplier(GROUND, listOf(ROCK), null, TypeChart.EMPTY)

        assertEquals(1.0, result, delta)
    }

    @Test
    fun `levitate removes ground weakness`() {
        val chart = chartOf(Triple(GROUND, ROCK, 2.0))

        val result = defensiveMultiplier(GROUND, listOf(ROCK), "levitate", chart)

        assertEquals(0.0, result, delta)
    }

    @Test
    fun `levitate does not affect other attacking types`() {
        val chart = chartOf(Triple(WATER, ROCK, 2.0))

        val result = defensiveMultiplier(WATER, listOf(ROCK), "levitate", chart)

        assertEquals(2.0, result, delta)
    }

    @Test
    fun `type becomes threat when enough members are weak to it`() {
        val chart = chartOf(
            Triple(GROUND, ELECTRIC, 2.0),
            Triple(GROUND, ROCK, 2.0),
        )
        val team = listOf(
            TeamMember(1, "pikachu", listOf(ELECTRIC)),
            TeamMember(2, "geodude", listOf(ROCK)),
            TeamMember(3, "pidgey", listOf(FLYING)),
        )

        val coverage = analyzeTeam(team, chart, threatThreshold = 2)

        val ground = coverage.threats.single { it.type == GROUND }
        assertEquals(listOf("pikachu", "geodude"), ground.weakMembers)
        assertEquals(2.0, ground.maxMultiplier, delta)
    }

    @Test
    fun `type is not a threat below threshold`() {
        val chart = chartOf(
            Triple(GROUND, ELECTRIC, 2.0),
            Triple(GROUND, ROCK, 2.0),
        )
        val team = listOf(
            TeamMember(1, "pikachu", listOf(ELECTRIC)),
            TeamMember(2, "geodude", listOf(ROCK)),
            TeamMember(3, "pidgey", listOf(FLYING)),
        )

        val coverage = analyzeTeam(team, chart, threatThreshold = 3)

        assertTrue(coverage.threats.none { it.type == GROUND })
    }

    @Test
    fun `member with ability immunity counts as resistant`() {
        val chart = chartOf(Triple(GROUND, ROCK, 2.0))
        val team = listOf(
            TeamMember(1, "geodude", listOf(ROCK)),
            TeamMember(2, "bronzong", listOf(ROCK), abilityName = "levitate"),
        )

        val coverage = analyzeTeam(team, chart, threatThreshold = 1)

        val ground = coverage.exposures.single { it.type == GROUND }
        assertEquals(listOf("geodude"), ground.weakMembers)
        assertEquals(listOf("bronzong"), ground.resistantMembers)
    }

    @Test
    fun `threats are sorted by number of weak members`() {
        val chart = chartOf(
            Triple(GROUND, ROCK, 2.0),
            Triple(WATER, ROCK, 2.0),
            Triple(WATER, FIRE, 2.0),
        )
        val team = listOf(
            TeamMember(1, "geodude", listOf(ROCK)),
            TeamMember(2, "charmander", listOf(FIRE)),
        )

        val coverage = analyzeTeam(team, chart, threatThreshold = 1)

        assertEquals(WATER, coverage.threats.first().type)
    }

    @Test
    fun `coverage reports every type`() {
        val coverage = analyzeTeam(emptyList(), TypeChart.EMPTY, threatThreshold = 1)

        assertEquals(PokeType.entries.size, coverage.exposures.size)
    }
}
