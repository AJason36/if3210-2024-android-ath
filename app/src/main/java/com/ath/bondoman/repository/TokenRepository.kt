package com.ath.bondoman.repository

import androidx.datastore.preferences.core.edit
import com.ath.bondoman.data.datastore.TokenDataStore
import com.ath.bondoman.di.datastore
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow

class TokenRepository @Inject constructor(
    private val tokenDataStore: TokenDataStore,
) {
    fun getToken(): Flow<String?>  {
        return tokenDataStore.getToken()
    }

    suspend fun saveToken(token: String) {
        tokenDataStore.saveToken(token)
    }

    suspend fun removeToken() {
        tokenDataStore.removeToken()
    }
}