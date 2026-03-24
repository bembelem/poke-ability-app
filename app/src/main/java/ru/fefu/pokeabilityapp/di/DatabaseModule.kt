package ru.fefu.pokeabilityapp.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.fefu.pokeabilityapp.data.local.AppDatabase
import ru.fefu.pokeabilityapp.data.local.FavouriteDao
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
        ).build()

    @Provides
    @Singleton
    fun provideFavouriteDao(db: AppDatabase): FavouriteDao =
        db.getFavouriteDao()
}