package ru.fefu.pokeabilityapp.ui.teams

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import ru.fefu.pokeabilityapp.MainDispatcherRule
import ru.fefu.pokeabilityapp.domain.model.PokeType
import ru.fefu.pokeabilityapp.domain.model.PokeType.FIRE
import ru.fefu.pokeabilityapp.domain.model.PokeType.FLYING
import ru.fefu.pokeabilityapp.domain.model.PokeType.GROUND
import ru.fefu.pokeabilityapp.domain.model.PokeType.ROCK
import ru.fefu.pokeabilityapp.domain.model.PokeType.WATER
import ru.fefu.pokeabilityapp.domain.model.PokemonAbilityOption
import ru.fefu.pokeabilityapp.domain.model.PokemonDetail
import ru.fefu.pokeabilityapp.domain.model.Team
import ru.fefu.pokeabilityapp.domain.model.TeamSlot
import ru.fefu.pokeabilityapp.domain.model.TypeChart
import ru.fefu.pokeabilityapp.fake.FakePokemonRepository
import ru.fefu.pokeabilityapp.fake.FakeTeamRepository

@OptIn(ExperimentalCoroutinesApi::class)
class TeamEditorViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun slot(id: Long, position: Int, pokemonId: Int, name: String) = TeamSlot(
        id = id,
        position = position,
        pokemonId = pokemonId,
        pokemonName = name,
        abilityId = null,
        abilityName = null
    )

    private fun team(vararg slots: TeamSlot) = Team(id = 1, name = "main", slots = slots.toList())

    private fun detail(id: Int, name: String, vararg types: PokeType) =
        PokemonDetail(id, name, spriteUrl = null, types = types.toList(), abilities = emptyList())

    private fun chartOf(vararg relations: Triple<PokeType, PokeType, Double>) = TypeChart(
        relations.associate { (attacking, defending, multiplier) ->
            (attacking to defending) to multiplier
        }
    )

    // uiState собран через WhileSubscribed, без подписчика value не обновляется
    private fun TestScope.createViewModel(
        teams: FakeTeamRepository,
        pokemon: FakePokemonRepository
    ): TeamEditorViewModel {
        val viewModel = TeamEditorViewModel(teams, pokemon, SavedStateHandle(mapOf("teamId" to 1L)))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.uiState.collect { }
        }
        return viewModel
    }

    @Test
    fun `editor always shows six slots in position order`() = runTest {
        val teams = FakeTeamRepository(team(slot(10, 0, 6, "charizard"), slot(11, 3, 7, "squirtle")))
        val viewModel = createViewModel(teams, FakePokemonRepository())
        advanceUntilIdle()

        val names = viewModel.uiState.value.slots.map { it.pokemonName }

        assertEquals(listOf("charizard", null, null, "squirtle", null, null), names)
    }

    @Test
    fun `coverage waits for type chart`() = runTest {
        val teams = FakeTeamRepository(team(slot(10, 0, 6, "charizard")))
        val pokemon = FakePokemonRepository().apply {
            details.value = listOf(detail(6, "charizard", FIRE, FLYING))
        }
        val viewModel = createViewModel(teams, pokemon)
        advanceUntilIdle()

        val state = viewModel.uiState.value
        assertFalse(state.chartReady)
        assertNull(state.coverage)
    }

    @Test
    fun `three fire pokemon make water a threat`() = runTest {
        val teams = FakeTeamRepository(
            team(
                slot(10, 0, 4, "charmander"),
                slot(11, 1, 5, "charmeleon"),
                slot(12, 2, 6, "charizard")
            )
        )
        val pokemon = FakePokemonRepository().apply {
            details.value = listOf(
                detail(4, "charmander", FIRE),
                detail(5, "charmeleon", FIRE),
                detail(6, "charizard", FIRE, FLYING)
            )
            chart.value = chartOf(Triple(WATER, FIRE, 2.0))
        }
        val viewModel = createViewModel(teams, pokemon)
        advanceUntilIdle()

        val water = viewModel.uiState.value.coverage!!.threats.single { it.type == WATER }
        assertEquals(3, water.weakMembers.size)
    }

    @Test
    fun `choosing levitate removes ground weakness`() = runTest {
        val teams = FakeTeamRepository(team(slot(10, 0, 74, "geodude")))
        val pokemon = FakePokemonRepository().apply {
            details.value = listOf(detail(74, "geodude", ROCK))
            chart.value = chartOf(Triple(GROUND, ROCK, 2.0))
        }
        val viewModel = createViewModel(teams, pokemon)
        advanceUntilIdle()

        fun groundMultiplier() = viewModel.uiState.value.coverage!!.exposures
            .single { it.type == GROUND }.perMember.single().multiplier

        assertEquals(2.0, groundMultiplier(), 0.0001)

        viewModel.selectAbility(10, PokemonAbilityOption(26, "levitate", isHidden = false))
        advanceUntilIdle()

        assertEquals(0.0, groundMultiplier(), 0.0001)
    }

    @Test
    fun `details are requested once per pokemon`() = runTest {
        val teams = FakeTeamRepository(team(slot(10, 0, 6, "charizard")))
        val pokemon = FakePokemonRepository()
        val viewModel = createViewModel(teams, pokemon)
        advanceUntilIdle()

        viewModel.selectAbility(10, PokemonAbilityOption(66, "blaze", isHidden = false))
        advanceUntilIdle()

        assertEquals(listOf(6), pokemon.ensureDetailCalls)
    }

    @Test
    fun `clearing slot frees its position`() = runTest {
        val teams = FakeTeamRepository(team(slot(10, 0, 6, "charizard")))
        val viewModel = createViewModel(teams, FakePokemonRepository())
        advanceUntilIdle()

        viewModel.clearSlot(0)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.slots.all { it.pokemonName == null })
    }
}
