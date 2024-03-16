package com.ath.bondoman.di

import com.ath.bondoman.api.AuthClient
import com.ath.bondoman.data.datastore.TokenDataStore
import com.ath.bondoman.repository.TokenRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Retrofit
import javax.inject.Singleton


// put dependencies whose lifecycle is tied to a view model
@Module
@InstallIn(ViewModelComponent::class)
class HiltModule {
    @Provides
    fun provideTokenRepository(tokenDataStore: TokenDataStore): TokenRepository = TokenRepository(tokenDataStore)

    @Provides
    fun provideAuthClient(retrofit: Retrofit.Builder): AuthClient =
        retrofit
            .build()
            .create(AuthClient::class.java)
}