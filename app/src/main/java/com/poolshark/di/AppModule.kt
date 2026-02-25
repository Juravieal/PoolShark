package com.poolshark.di

import com.poolshark.domain.shot.ShotCalculator
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides @Singleton
    fun provideShotCalculator(): ShotCalculator = ShotCalculator()
}
