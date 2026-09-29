package ru.fefu.pokeabilityapp.domain.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CachePolicyTest {

    private val ttl = hoursToMillis(24)
    private val now = 1_000_000_000L

    @Test
    fun `never fetched is stale`() {
        assertTrue(isStale(fetchedAt = null, ttlMillis = ttl, now = now))
    }

    @Test
    fun `just fetched is fresh`() {
        assertFalse(isStale(fetchedAt = now, ttlMillis = ttl, now = now))
    }

    @Test
    fun `older than ttl is stale`() {
        assertTrue(isStale(fetchedAt = now - ttl - 1, ttlMillis = ttl, now = now))
    }

    @Test
    fun `exactly ttl old is stale`() {
        assertTrue(isStale(fetchedAt = now - ttl, ttlMillis = ttl, now = now))
    }

    @Test
    fun `just under ttl is fresh`() {
        assertFalse(isStale(fetchedAt = now - ttl + 1, ttlMillis = ttl, now = now))
    }

    @Test
    fun `hoursToMillis converts hours`() {
        assertEquals(3_600_000L, hoursToMillis(1))
        assertEquals(86_400_000L, hoursToMillis(24))
    }
}
