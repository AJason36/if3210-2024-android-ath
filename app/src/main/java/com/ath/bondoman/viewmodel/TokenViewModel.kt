package com.ath.bondoman.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ath.bondoman.data.datastore.TokenDataStore
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

    val token = MutableLiveData<String?>()

    init {
        // get token on initialization on dispatchers.io scope
        viewModelScope.launch(Dispatchers.IO) {
            tokenRepository.getToken().collect {
                withContext(Dispatchers.Main) {
                    token.value = it
                }
            }
        }
    }

    fun saveToken(token: String) {
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