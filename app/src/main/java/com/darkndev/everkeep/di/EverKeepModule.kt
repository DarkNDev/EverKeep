package com.darkndev.everkeep.di

import android.app.Application
import androidx.room.Room
import com.darkndev.everkeep.database.EverKeepDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object EverKeepModule {

    @Provides
    @Singleton
    fun provideDatabase(
        app: Application,
        callback: EverKeepDatabase.Callback
    ) = Room.databaseBuilder(app, EverKeepDatabase::class.java, "everKeep_database")
        .fallbackToDestructiveMigration()
        .addCallback(callback)
        .build()

    @Provides
    fun provideItemDao(database: EverKeepDatabase) = database.noteDao()

    @Provides
    fun provideLabelDao(database: EverKeepDatabase) = database.labelDao()

    @EverKeepScope
    @Provides
    @Singleton
    fun provideEverKeepScope() = CoroutineScope(SupervisorJob())
}