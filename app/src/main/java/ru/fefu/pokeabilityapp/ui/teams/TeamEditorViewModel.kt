package ru.fefu.pokeabilityapp.ui.teams

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import ru.fefu.pokeabilityapp.domain.coverage.THREAT_THRESHOLD
import ru.fefu.pokeabilityapp.domain.coverage.TeamCoverage
import ru.fefu.pokeabilityapp.domain.coverage.TeamMember
import ru.fefu.pokeabilityapp.domain.coverage.analyzeTeam
import ru.fefu.pokeabilityapp.domain.model.PokeType
import ru.fefu.pokeabilityapp.domain.model.PokemonAbilityOption
import ru.fefu.pokeabilityapp.domain.model.PokemonDetail
import ru.fefu.pokeabilityapp.domain.model.TEAM_SIZE
import ru.fefu.pokeabilityapp.domain.model.Team
import ru.fefu.pokeabilityapp.domain.model.TypeChart
import ru.fefu.pokeabilityapp.domain.repository.PokemonRepository
import ru.fefu.pokeabilityapp.domain.repository.TeamRepository
import javax.inject.Inject

data class SlotUi(
    val position: Int,
    val slotId: Long?,
    val pokemonName: String?,
    val spriteUrl: String?,
    val types: List<PokeType>,
    val abilityOptions: List<PokemonAbilityOption>,
    val selectedAbilityId: Int?
)

data class TeamEditorUiState(
    val teamName: String = "",
    val slots: List<SlotUi> = emptyList(),
    val coverage: TeamCoverage? = null,
    val chartReady: Boolean = false
)

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class TeamEditorViewModel @Inject constructor(
    private val teamRepository: TeamRepository,
    private val pokemonRepository: PokemonRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val teamId: Long = checkNotNull(savedStateHandle["teamId"])

    private val teamFlow = teamRepository.observeTeam(teamId)

    private val detailsFlow = teamFlow.flatMapLatest { team ->
        val ids = team?.slots?.map { it.pokemonId }.orEmpty().distinct()
        if (ids.isEmpty()) flowOf(emptyList()) else pokemonRepository.observeDetails(ids)
    }

    init {
        viewModelScope.launch {
            runCatching { pokemonRepository.refreshTypeChart(force = false) }
        }
        // реагируем только на смену состава, иначе правка updatedAt тоже дёргает загрузку
        viewModelScope.launch {
            teamFlow
                .map { team -> team?.slots?.map { it.pokemonId }.orEmpty().distinct() }
                .distinctUntilChanged()
                .collect { ids ->
                    ids.forEach { runCatching { pokemonRepository.ensureDetail(it) } }
                }
        }
    }

    val uiState: StateFlow<TeamEditorUiState> = combine(
        teamFlow,
        detailsFlow,
        pokemonRepository.observeTypeChart()
    ) { team, details, chart ->
        val detailsById = details.associateBy { it.id }
        TeamEditorUiState(
            teamName = team?.name.orEmpty(),
            slots = buildSlots(team, detailsById),
            coverage = buildCoverage(team, detailsById, chart),
            chartReady = !chart.isEmpty
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TeamEditorUiState()
    )

    private fun buildSlots(
        team: Team?,
        detailsById: Map<Int, PokemonDetail>
    ): List<SlotUi> = (0 until TEAM_SIZE).map { position ->
        val slot = team?.slotAt(position)
        val detail = slot?.let { detailsById[it.pokemonId] }
        SlotUi(
            position = position,
            slotId = slot?.id,
            pokemonName = slot?.pokemonName,
            spriteUrl = detail?.spriteUrl,
            types = detail?.types.orEmpty(),
            abilityOptions = detail?.abilities.orEmpty(),
            selectedAbilityId = slot?.abilityId
        )
    }

    private fun buildCoverage(
        team: Team?,
        detailsById: Map<Int, PokemonDetail>,
        chart: TypeChart
    ): TeamCoverage? {
        if (team == null || chart.isEmpty) return null
        val members = team.slots.mapNotNull { slot ->
            val detail = detailsById[slot.pokemonId] ?: return@mapNotNull null
            if (detail.types.isEmpty()) return@mapNotNull null
            TeamMember(
                slotId = slot.id,
                pokemonName = slot.nickname ?: detail.name,
                types = detail.types,
                abilityName = slot.abilityName
            )
        }
        if (members.isEmpty()) return null
        return analyzeTeam(members, chart, THREAT_THRESHOLD)
    }

    fun selectAbility(slotId: Long, option: PokemonAbilityOption?) {
        viewModelScope.launch {
            teamRepository.setSlotAbility(slotId, option?.abilityId, option?.name)
        }
    }

    fun clearSlot(position: Int) {
        viewModelScope.launch { teamRepository.clearSlot(teamId, position) }
    }
}
