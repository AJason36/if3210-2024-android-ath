package com.ath.bondoman.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.ath.bondoman.di.datastore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class TokenDataStore(private val context: Context) {
    companion object {
        private val TOKEN_KEY = stringPreferencesKey("auth_token")
    }
    // for now we only use one datastore (in the context singleton) which 8is the token datastore
    fun getToken(): Flow<String?> {
        return context.datastore.data.map { preferences ->
            preferences[TOKEN_KEY]
        }
    }

    suspend fun saveToken(token: String) {
        context.datastore.edit { preferences ->
            preferences[TOKEN_KEY] = token
        }
    }

    suspend fun removeToken() {
        context.datastore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }
}