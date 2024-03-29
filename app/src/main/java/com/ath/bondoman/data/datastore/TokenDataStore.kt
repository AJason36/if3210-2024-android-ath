package com.ath.bondoman.data.datastore

import android.content.Context
import android.util.Log
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ath.bondoman.di.datastore
import com.ath.bondoman.model.Token
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class TokenDataStore(private val context: Context) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
    }

    private val gson = Gson()

    fun getToken(): Flow<Token?> {
        return context.datastore.data.map { preferences ->
            val tokenString = preferences[TOKEN_KEY]
            Log.d("AUTH", tokenString.toString())
            tokenString?.let { gson.fromJson(it, Token::class.java) }
        }
    }

    suspend fun saveToken(token: Token) {
        val tokenString = gson.toJson(token)
        Log.d("TOKEN STRING", tokenString)
        context.datastore.edit { preferences ->
            preferences[TOKEN_KEY] = tokenString
        }
    }

    suspend fun removeToken() {
        context.datastore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }
}
