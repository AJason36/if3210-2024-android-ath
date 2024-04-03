package com.ath.bondoman.repository
import com.ath.bondoman.data.datastore.TokenDataStore
import com.ath.bondoman.model.Token
import javax.inject.Inject

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