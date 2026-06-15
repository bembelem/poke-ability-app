package ru.fefu.pokeabilityapp.data.repository

import kotlinx.coroutines.runBlocking
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertNull
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import ru.fefu.pokeabilityapp.data.dto.AbilityDto
import ru.fefu.pokeabilityapp.data.dto.AbilityListDto
import ru.fefu.pokeabilityapp.data.service.PokeApiService
import java.io.IOException

class AbilityRepositoryImplTest {

    private class FailingApi(private val error: Throwable) : PokeApiService {
        override suspend fun getAbilities(limit: Int, offset: Int): AbilityListDto = throw error
        override suspend fun getAbilityById(id: Int): AbilityDto = throw error
        override suspend fun getAbilityByName(name: String): AbilityDto = throw error
    }

    private fun httpError(code: Int) =
        HttpException(Response.error<Any>(code, "".toResponseBody(null)))

    @Test
    fun `getAbilityByName returns null when ability does not exist`() = runBlocking {
        val repository = AbilityRepositoryImpl(FailingApi(httpError(404)))

        assertNull(repository.getAbilityByName("no-such-ability"))
    }

    @Test(expected = IOException::class)
    fun `getAbilityByName rethrows network failure`() = runBlocking {
        val repository = AbilityRepositoryImpl(FailingApi(IOException("offline")))

        repository.getAbilityByName("overgrow")
        Unit
    }

    @Test(expected = HttpException::class)
    fun `getAbilityByName rethrows server error`() = runBlocking {
        val repository = AbilityRepositoryImpl(FailingApi(httpError(500)))

        repository.getAbilityByName("overgrow")
        Unit
    }
}
