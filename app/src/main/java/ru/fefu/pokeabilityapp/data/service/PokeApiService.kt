package ru.fefu.pokeabilityapp.data.service

import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import ru.fefu.pokeabilityapp.data.dto.AbilityDto
import ru.fefu.pokeabilityapp.data.dto.AbilityListDto

interface PokeApiService {
    @GET("ability")
    suspend fun getAbilities(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): AbilityListDto

    @GET("ability/{id}")
    suspend fun getAbilityById(@Path("id") id: Int): AbilityDto
}