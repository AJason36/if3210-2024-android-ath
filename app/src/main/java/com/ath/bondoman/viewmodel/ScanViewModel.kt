package com.ath.bondoman.viewmodel

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.ath.bondoman.api.UploadClient
import com.ath.bondoman.model.Transaction
import com.ath.bondoman.model.TransactionCategory
import com.ath.bondoman.model.dto.ApiResponse
import com.ath.bondoman.model.dto.Item
import com.ath.bondoman.model.dto.UploadResponse
import com.ath.bondoman.repository.TokenRepository
import com.ath.bondoman.util.apiRequestFlow
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.onCompletion
import okhttp3.MultipartBody
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val uploadClient: UploadClient,
    private val tokenRepository: TokenRepository
): BaseViewModel() {
    private val _imageBitmap = MutableLiveData<Bitmap>()
    val imageBitmap: LiveData<Bitmap> = _imageBitmap

    private val _uploadResponse= MutableLiveData<ApiResponse<UploadResponse>>()
    val uploadResponse = _uploadResponse

    private val _text = MutableLiveData<String>().apply {
        value = "This is scanner Fragment"
    }
    val text: LiveData<String> = _text
    fun updateImageBitmap(bitmap: Bitmap) {
        _imageBitmap.value = bitmap
    }

    fun upload(token:String,payload:MultipartBody.Part,coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(
        uploadResponse,
        coroutinesErrorHandler
    ){
        Log.d("NetworkCall", "Token: $token")
        Log.d("NetworkCall", "Request Body: $payload")

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

    fun createTransactionFromItems(items: List<Item>): Transaction {
        val totalAmount = items.sumOf { it.qty * it.price }
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH:mm:ss", Locale.getDefault())
        val currentDate = dateFormat.format(Date())
        val transactionName = "Transaction_$currentDate"

        return Transaction(
            title = transactionName,
            amount = totalAmount,
            location = null,
            category = TransactionCategory.Expenditure,
            date = currentDate.split("_")[0],
            userEmail = tokenRepository.getToken()?.email ?: ""
        )
    }
}