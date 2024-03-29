package com.ath.bondoman.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ath.bondoman.model.Transaction
import com.ath.bondoman.model.dto.InsertTransactionDTO
import com.ath.bondoman.model.dto.UpdateTransactionDTO
import com.ath.bondoman.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(private val repository: TransactionRepository) : ViewModel() {
    private val _text = MutableLiveData<String>().apply {
        value = "This is transaction Fragment"
    }
    val text: LiveData<String> = _text

    val allTransactions: LiveData<List<Transaction>> = repository.allTransactions

    private val _insertResult = MutableLiveData<Long>()
    val insertResult: LiveData<Long> = _insertResult
    private val _updateResult = MutableLiveData<Int>()
    val updateResult: LiveData<Int> = _updateResult

    fun insertTransaction(transactionDTO: InsertTransactionDTO) {
        viewModelScope.launch {
            val transaction = Transaction(
                title = transactionDTO.title,
                category = transactionDTO.category,
                amount = transactionDTO.amount,
                location = transactionDTO.location
            )
            val rowId = withContext(Dispatchers.IO) {
                repository.insert(transaction)
            }
            _insertResult.value = rowId

        }
    }

    fun updateTransaction(transactionDTO: UpdateTransactionDTO) {
        viewModelScope.launch {
            val transaction = Transaction(
                id = transactionDTO.id,
                title = transactionDTO.title,
                category = transactionDTO.category,
                amount = transactionDTO.amount,
                location = transactionDTO.location,
                date = transactionDTO.date
            )
            val rowId = withContext(Dispatchers.IO) {
                repository.update(transaction)
            }
            _updateResult.value = rowId
        }
    }
}
