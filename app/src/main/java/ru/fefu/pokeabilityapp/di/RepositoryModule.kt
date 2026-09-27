package ru.fefu.pokeabilityapp.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import ru.fefu.pokeabilityapp.data.repository.AbilityRepositoryImpl
import ru.fefu.pokeabilityapp.data.repository.FavouriteRepositoryImpl
import ru.fefu.pokeabilityapp.data.repository.ProfileRepositoryImpl
import ru.fefu.pokeabilityapp.data.repository.SettingsRepositoryImpl
import ru.fefu.pokeabilityapp.data.repository.TagRepositoryImpl
import ru.fefu.pokeabilityapp.data.repository.TeamRepositoryImpl
import ru.fefu.pokeabilityapp.domain.repository.AbilityRepository
import ru.fefu.pokeabilityapp.domain.repository.FavouriteRepository
import ru.fefu.pokeabilityapp.domain.repository.ProfileRepository
import ru.fefu.pokeabilityapp.domain.repository.SettingsRepository
import ru.fefu.pokeabilityapp.domain.repository.TagRepository
import ru.fefu.pokeabilityapp.domain.repository.TeamRepository
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

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ): SettingsRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(
        impl: ProfileRepositoryImpl
    ): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindTeamRepository(
        impl: TeamRepositoryImpl
    ): TeamRepository

    @Binds
    @Singleton
    abstract fun bindTagRepository(
        impl: TagRepositoryImpl
    ): TagRepository
}
