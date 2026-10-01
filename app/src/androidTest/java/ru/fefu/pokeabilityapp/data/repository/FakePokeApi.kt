package ru.fefu.pokeabilityapp.data.repository

import ru.fefu.pokeabilityapp.data.dto.AbilityDto
import ru.fefu.pokeabilityapp.data.dto.AbilityEntryDto
import ru.fefu.pokeabilityapp.data.dto.AbilityListDto
import ru.fefu.pokeabilityapp.data.dto.DamageRelationsDto
import ru.fefu.pokeabilityapp.data.dto.EffectEntryDto
import ru.fefu.pokeabilityapp.data.dto.NamedResourceDto
import ru.fefu.pokeabilityapp.data.dto.PokemonAbilitySlotDto
import ru.fefu.pokeabilityapp.data.dto.PokemonDto
import ru.fefu.pokeabilityapp.data.dto.PokemonListDto
import ru.fefu.pokeabilityapp.data.dto.PokemonTypeSlotDto
import ru.fefu.pokeabilityapp.data.dto.SpritesDto
import ru.fefu.pokeabilityapp.data.dto.TypeDto
import ru.fefu.pokeabilityapp.data.service.PokeApiService

class FakePokeApi : PokeApiService {

    var listResponse: AbilityListDto = AbilityListDto(0, null, null, emptyList())
    var detailResponse: AbilityDto? = null
    var pokemonListResponse: PokemonListDto = PokemonListDto(0, emptyList())
    var pokemonResponse: PokemonDto? = null
    var typeResponses: Map<Int, TypeDto> = emptyMap()
    var failure: Throwable? = null

    var listCalls = 0
        private set
    var detailCalls = 0
        private set
    var byNameCalls = 0
        private set
    var pokemonListCalls = 0
        private set
    var pokemonCalls = 0
        private set
    var typeCalls = 0
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

    override suspend fun getPokemonList(limit: Int, offset: Int): PokemonListDto {
        pokemonListCalls++
        failure?.let { throw it }
        return pokemonListResponse
    }

    override suspend fun getPokemonById(id: Int): PokemonDto {
        pokemonCalls++
        failure?.let { throw it }
        return pokemonResponse ?: throw IllegalStateException("pokemonResponse not set")
    }

    override suspend fun getType(id: Int): TypeDto {
        typeCalls++
        failure?.let { throw it }
        return typeResponses[id] ?: typeDto(id, "type-$id")
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

fun pokemonListDto(vararg idToName: Pair<Int, String>): PokemonListDto = PokemonListDto(
    count = idToName.size,
    results = idToName.map { (id, name) ->
        NamedResourceDto(name = name, url = "https://pokeapi.co/api/v2/pokemon/$id/")
    }
)

fun pokemonDto(
    id: Int,
    name: String,
    types: List<String>,
    abilities: List<Pair<Int, String>> = emptyList()
): PokemonDto = PokemonDto(
    id = id,
    name = name,
    sprites = SpritesDto(frontDefault = "https://img/$id.png"),
    types = types.mapIndexed { index, type ->
        PokemonTypeSlotDto(
            slot = index + 1,
            type = NamedResourceDto(type, "https://pokeapi.co/api/v2/type/$type/")
        )
    },
    abilities = abilities.mapIndexed { index, (abilityId, abilityName) ->
        PokemonAbilitySlotDto(
            isHidden = index > 0,
            slot = index + 1,
            ability = NamedResourceDto(
                abilityName,
                "https://pokeapi.co/api/v2/ability/$abilityId/"
            )
        )
    }
)

fun typeDto(
    id: Int,
    name: String,
    doubleDamageFrom: List<String> = emptyList(),
    halfDamageFrom: List<String> = emptyList(),
    noDamageFrom: List<String> = emptyList()
): TypeDto = TypeDto(
    id = id,
    name = name,
    damageRelations = DamageRelationsDto(
        doubleDamageFrom = doubleDamageFrom.map { NamedResourceDto(it, "") },
        halfDamageFrom = halfDamageFrom.map { NamedResourceDto(it, "") },
        noDamageFrom = noDamageFrom.map { NamedResourceDto(it, "") }
    )
)
