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
    fun getToken(): Token? {
        return tokenDataStore.getToken()
    }

    fun saveToken(token: Token) {
        tokenDataStore.saveToken(token)
    }

    fun removeToken() {
        tokenDataStore.removeToken()
    }
}