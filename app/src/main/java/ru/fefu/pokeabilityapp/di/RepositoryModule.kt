package ru.fefu.pokeabilityapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.fefu.pokeabilityapp.data.repository.AbilityRepositoryImpl
import ru.fefu.pokeabilityapp.data.repository.FavouriteRepositoryImpl
import ru.fefu.pokeabilityapp.domain.repository.AbilityRepository
import ru.fefu.pokeabilityapp.domain.repository.FavouriteRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAbilityRepository(
        impl: AbilityRepositoryImpl
    ): AbilityRepository

    @Binds
    @Singleton
    abstract fun bindFavouriteRepository(
        impl: FavouriteRepositoryImpl
    ): FavouriteRepository
}
