package ru.fefu.pokeabilityapp

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import ru.fefu.pokeabilityapp.domain.model.PokemonDetail
import ru.fefu.pokeabilityapp.domain.model.PokemonItem
import ru.fefu.pokeabilityapp.domain.model.TypeChart
import ru.fefu.pokeabilityapp.domain.repository.PokemonRepository

class FakePokemonRepository : PokemonRepository {

    val details = MutableStateFlow<List<PokemonDetail>>(emptyList())
    val chart = MutableStateFlow(TypeChart.EMPTY)
    val ensureDetailCalls = mutableListOf<Int>()
    var failure: Throwable? = null

    var refreshFirstPageCalls = 0
        private set
    var refreshTypeChartCalls = 0
        private set

    override fun observePokemon(): Flow<List<PokemonItem>> = flowOf(emptyList())

    override fun observeDetails(ids: List<Int>): Flow<List<PokemonDetail>> =
        details.map { list -> list.filter { it.id in ids } }

    override fun observeTypeChart(): Flow<TypeChart> = chart

    override suspend fun refreshFirstPage(force: Boolean) {
        failure?.let { throw it }
        refreshFirstPageCalls++
    }

    override suspend fun loadNextPage(): Int = 0

    override suspend fun ensureDetail(id: Int) {
        failure?.let { throw it }
        ensureDetailCalls += id
    }

    override suspend fun refreshTypeChart(force: Boolean) {
        failure?.let { throw it }
        refreshTypeChartCalls++
    }
}
