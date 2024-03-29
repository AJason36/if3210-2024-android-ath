package com.ath.bondoman.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ath.bondoman.data.datastore.TokenDataStore
import com.ath.bondoman.model.Token
import com.ath.bondoman.repository.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TokenViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
): ViewModel() {

    val token = tokenRepository.getToken().asLiveData(Dispatchers.IO)

    fun saveToken(token: Token) {
        viewModelScope.launch(Dispatchers.IO) {
            tokenRepository.saveToken(token)
        }
    }

    fun removeToken() {
        viewModelScope.launch(Dispatchers.IO) {
            tokenRepository.removeToken()
        }
    }
}