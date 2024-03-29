package com.ath.bondoman.repository

import androidx.datastore.preferences.core.edit
import com.ath.bondoman.data.datastore.TokenDataStore
import com.ath.bondoman.di.datastore
import com.ath.bondoman.model.Token
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class TokenRepository @Inject constructor(
    private val tokenDataStore: TokenDataStore,
) {
    fun getToken(): Flow<Token?>  {
        return tokenDataStore.getToken()
    }

    suspend fun saveToken(token: Token) {
        tokenDataStore.saveToken(token)
    }

    suspend fun removeToken() {
        tokenDataStore.removeToken()
    }
}