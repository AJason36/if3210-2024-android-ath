package com.ath.bondoman.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.ath.bondoman.repository.TokenRepository
import com.ath.bondoman.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class ChartViewModel @Inject constructor(private val repository: TransactionRepository, private val tokenRepository: TokenRepository) : ViewModel(){
    private val userEmail = tokenRepository.getToken()?.email ?: ""
    private val _text = MutableLiveData<String>().apply {
        value = "There is no transaction"
    }
    val text: LiveData<String> = _text
    val allIncome: LiveData<Double> = repository.getAllIncome(userEmail).asLiveData(Dispatchers.IO)
    val allExpenditure: LiveData<Double> = repository.getAllExpenditure(userEmail).asLiveData(Dispatchers.IO)
}