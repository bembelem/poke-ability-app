package ru.fefu.pokeabilityapp.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.fefu.pokeabilityapp.data.local.AbilityCacheDao
import ru.fefu.pokeabilityapp.data.local.AppDatabase
import ru.fefu.pokeabilityapp.data.local.FavouriteDao
import ru.fefu.pokeabilityapp.data.local.HistoryDao
import ru.fefu.pokeabilityapp.data.local.MIGRATION_1_2
import ru.fefu.pokeabilityapp.data.local.MIGRATION_2_3
import ru.fefu.pokeabilityapp.data.local.MIGRATION_3_4
import ru.fefu.pokeabilityapp.data.local.MIGRATION_4_5
import ru.fefu.pokeabilityapp.data.local.MIGRATION_5_6
import ru.fefu.pokeabilityapp.data.local.ProfileDao
import ru.fefu.pokeabilityapp.data.local.TagDao
import ru.fefu.pokeabilityapp.data.local.TeamDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "pokeability.db"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6)
            .build()

    @Provides
    @Singleton
    fun provideFavouriteDao(db: AppDatabase): FavouriteDao =
        db.getFavouriteDao()

    @Provides
    @Singleton
    fun provideProfileDao(db: AppDatabase): ProfileDao =
        db.getProfileDao()

    @Provides
    @Singleton
    fun provideTeamDao(db: AppDatabase): TeamDao =
        db.getTeamDao()

    @Provides
    @Singleton
    fun provideTagDao(db: AppDatabase): TagDao =
        db.getTagDao()

    @Provides
    @Singleton
    fun provideAbilityCacheDao(db: AppDatabase): AbilityCacheDao =
        db.getAbilityCacheDao()

    @Provides
    @Singleton
    fun provideHistoryDao(db: AppDatabase): HistoryDao =
        db.getHistoryDao()
}
