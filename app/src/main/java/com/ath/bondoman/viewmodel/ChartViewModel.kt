package com.ath.bondoman.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.ath.bondoman.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import javax.inject.Inject

@HiltViewModel
class ChartViewModel @Inject constructor(private val repository: TransactionRepository) : ViewModel(){

    private val _text = MutableLiveData<String>().apply {
        value = "This is chart Fragment"
    }
    val text: LiveData<String> = _text
    val allIncome: LiveData<Double> = repository.getAllIncome().asLiveData(Dispatchers.IO)
    val allExpenditure: LiveData<Double> = repository.getAllExpenditure().asLiveData(Dispatchers.IO)
}