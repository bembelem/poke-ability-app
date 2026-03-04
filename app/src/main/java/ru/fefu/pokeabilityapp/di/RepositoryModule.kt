package ru.fefu.pokeabilityapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.fefu.pokeabilityapp.data.repository.AbilityRepositoryImpl
import ru.fefu.pokeabilityapp.domain.repository.AbilityRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAbilityRepository(
        impl: AbilityRepositoryImpl
    ): AbilityRepository
}