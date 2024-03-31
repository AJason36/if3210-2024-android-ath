package com.ath.bondoman.data.datastore

import android.content.Context
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.ath.bondoman.model.Token
import com.google.gson.Gson

class TokenDataStore(private val context: Context) {
    companion object {
        private const val PREF_NAME = "encrypted_prefs"
        private const val TOKEN_KEY = "auth_token"
    }

    private val gson = Gson()

    private val masterKey: MasterKey by lazy {
        MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
    }

    private val sharedPreferences by lazy {
        EncryptedSharedPreferences.create(
            context,
            PREF_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    fun getToken(): Token? {
        val tokenString = sharedPreferences.getString(TOKEN_KEY, null)
        return tokenString?.let { gson.fromJson(it, Token::class.java) }
    }

    fun saveToken(token: Token) {
        val tokenString = gson.toJson(token)
        sharedPreferences.edit().putString(TOKEN_KEY, tokenString).apply()
    }

    fun removeToken() {
        sharedPreferences.edit().remove(TOKEN_KEY).apply()
    }
}
