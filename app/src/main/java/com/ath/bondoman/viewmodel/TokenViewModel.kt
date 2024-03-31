package com.ath.bondoman.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ath.bondoman.model.Token
import com.ath.bondoman.repository.TokenRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TokenViewModel @Inject constructor(
    private val tokenRepository: TokenRepository,
) : ViewModel() {

    private val _token = MutableLiveData<Token?>()
    val token: LiveData<Token?> = _token

    init {
        loadToken()
    }

    private fun loadToken() {
        viewModelScope.launch(Dispatchers.IO) {
            val loadedToken = tokenRepository.getToken()
            _token.postValue(loadedToken)
        }
    }

    fun saveToken(token: Token) {
        viewModelScope.launch(Dispatchers.IO) {
            tokenRepository.saveToken(token)
            _token.postValue(token)
        }
    }

    fun removeToken() {
        viewModelScope.launch(Dispatchers.IO) {
            tokenRepository.removeToken()
            _token.postValue(null)
        }
    }
}