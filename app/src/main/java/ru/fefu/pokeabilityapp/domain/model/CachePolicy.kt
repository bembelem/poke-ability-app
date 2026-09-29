package ru.fefu.pokeabilityapp.domain.model

fun hoursToMillis(hours: Int): Long = hours.toLong() * 60 * 60 * 1000

fun isStale(fetchedAt: Long?, ttlMillis: Long, now: Long): Boolean {
    if (fetchedAt == null) return true
    return now - fetchedAt >= ttlMillis
}
