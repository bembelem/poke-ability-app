package ru.fefu.pokeabilityapp.data.repository

import ru.fefu.pokeabilityapp.data.dto.AbilityDto
import ru.fefu.pokeabilityapp.data.dto.AbilityEntryDto
import ru.fefu.pokeabilityapp.data.dto.AbilityListDto
import ru.fefu.pokeabilityapp.data.dto.EffectEntryDto
import ru.fefu.pokeabilityapp.data.dto.NamedResourceDto
import ru.fefu.pokeabilityapp.data.service.PokeApiService

class FakePokeApi : PokeApiService {

    var listResponse: AbilityListDto = AbilityListDto(0, null, null, emptyList())
    var detailResponse: AbilityDto? = null
    var failure: Throwable? = null

    var listCalls = 0
        private set
    var detailCalls = 0
        private set
    var byNameCalls = 0
        private set

    override suspend fun getAbilities(limit: Int, offset: Int): AbilityListDto {
        listCalls++
        failure?.let { throw it }
        return listResponse
    }

    override suspend fun getAbilityById(id: Int): AbilityDto {
        detailCalls++
        failure?.let { throw it }
        return detailResponse ?: throw IllegalStateException("detailResponse not set")
    }

    override suspend fun getAbilityByName(name: String): AbilityDto {
        byNameCalls++
        failure?.let { throw it }
        return detailResponse ?: throw IllegalStateException("detailResponse not set")
    }
}

fun abilityListDto(vararg idToName: Pair<Int, String>): AbilityListDto = AbilityListDto(
    count = idToName.size,
    next = null,
    previous = null,
    results = idToName.map { (id, name) ->
        AbilityEntryDto(name = name, url = "https://pokeapi.co/api/v2/ability/$id/")
    }
)

fun abilityDto(id: Int, name: String): AbilityDto = AbilityDto(
    id = id,
    name = name,
    isMainSeries = true,
    generation = NamedResourceDto("generation-iii", ""),
    effectEntries = listOf(
        EffectEntryDto(
            effect = "full effect of $name",
            shortEffect = "short effect of $name",
            language = NamedResourceDto("en", "")
        )
    ),
    flavorTextEntries = emptyList(),
    pokemon = emptyList()
)
