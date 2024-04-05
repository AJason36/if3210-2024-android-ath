package com.ath.bondoman.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.ath.bondoman.api.UploadClient
import com.ath.bondoman.model.LocationData
import com.ath.bondoman.model.Transaction
import com.ath.bondoman.model.TransactionCategory
import com.ath.bondoman.model.dto.ApiResponse
import com.ath.bondoman.model.dto.InsertTransactionDTO
import com.ath.bondoman.model.dto.Item
import com.ath.bondoman.model.dto.UpdateTransactionDTO
import com.ath.bondoman.model.dto.UploadResponse
import com.ath.bondoman.repository.TokenRepository
import com.ath.bondoman.repository.TransactionRepository
import com.ath.bondoman.util.apiRequestFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class TransactionViewModel @Inject constructor(
    private val uploadClient: UploadClient,
    private val repository: TransactionRepository,
    private val tokenRepository: TokenRepository)
    : BaseViewModel() {
    private val userEmail = tokenRepository.getToken()?.email ?: ""

    private val _text = MutableLiveData<String>().apply {
        value = "This is transaction Fragment"
    }
    val text: LiveData<String> = _text

    val allTransactions: LiveData<List<Transaction>> = repository.getAll(userEmail).asLiveData(Dispatchers.IO)

    private val _insertResult = MutableLiveData<Long>()
    val insertResult: LiveData<Long> = _insertResult
    private val _updateResult = MutableLiveData<Int>()
    val updateResult: LiveData<Int> = _updateResult
    private val _deleteResult = MutableLiveData<Int>()
    val deleteResult: LiveData<Int> = _deleteResult

    private val _uploadResponse= MutableLiveData<ApiResponse<UploadResponse>>()
    val uploadResponse = _uploadResponse
    fun insertTransaction(transactionDTO: InsertTransactionDTO) {
        viewModelScope.launch {
            val transaction = Transaction(
                title = transactionDTO.title,
                category = transactionDTO.category,
                amount = transactionDTO.amount,
                location = transactionDTO.location,
                userEmail = userEmail
            )
            try {
                val rowId = withContext(Dispatchers.IO) {
                    repository.insert(transaction)
                }
                _insertResult.value = rowId
            } catch (e: Exception) {
                _insertResult.value = -1L
            }

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
                date = transactionDTO.date,
                userEmail = userEmail
            )
            val rowId = withContext(Dispatchers.IO) {
                repository.update(transaction)
            }
            _updateResult.value = rowId
        }
    }

    fun deleteTransaction(transaction: Transaction) {
        viewModelScope.launch {
            val rowId = withContext(Dispatchers.IO) {
                repository.delete(transaction)
            }
            _deleteResult.value = rowId
        }
    }
    fun upload(token:String, payload: MultipartBody.Part, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(
        uploadResponse,
        coroutinesErrorHandler
    ){

        apiRequestFlow { uploadClient.upload("Bearer $token", payload) }.onCompletion {
            // Log the response after the network call is completed
                cause ->
            // Log the response after the network call is completed
            if (cause != null) {
                Log.e("NetworkError", "Error message: ${cause.message}")
            } else {
                Log.d("NetworkResponse", "Response Code: ${_uploadResponse}")
            }

        }
    }
    fun createTransactionFromItems(items: List<Item>, location: LocationData): InsertTransactionDTO {
        val totalAmount = items.sumOf { it.qty * it.price }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        val transactionName = "Scan_$currentDate"

        return InsertTransactionDTO(
            title = transactionName,
            amount = totalAmount,
            location = location,
            category = TransactionCategory.Expenditure
        )
    }
}
