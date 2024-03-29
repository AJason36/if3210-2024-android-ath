package com.ath.bondoman.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ath.bondoman.model.Transaction
import com.ath.bondoman.model.dto.TransactionDTO
import com.ath.bondoman.repository.TransactionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
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

    fun insertTransaction(transactionDTO: TransactionDTO) {
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

            Log.d("TransactionViewModel", "Transaction inserted with row ID: $rowId")
        }
    }

    private fun isTransactionInserted(transaction: Transaction): Boolean {
        val transactions = allTransactions.value
        return transactions?.contains(transaction) ?: false
    }
}
