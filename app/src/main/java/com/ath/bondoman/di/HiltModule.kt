package com.ath.bondoman.di

import com.ath.bondoman.api.AuthClient
import com.ath.bondoman.data.database.AppDatabase
import com.ath.bondoman.data.datastore.TokenDataStore
import com.ath.bondoman.model.dao.TransactionDao
import com.ath.bondoman.repository.TokenRepository
import com.ath.bondoman.repository.TransactionRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ViewModelComponent
import retrofit2.Retrofit


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

    @Provides
    fun provideTransactionDao(database: AppDatabase): TransactionDao {
        return database.transactionDao()
    }

    @Provides
    fun provideTransactionRepository(transactionDao: TransactionDao): TransactionRepository {
        return TransactionRepository(transactionDao)
    }
}