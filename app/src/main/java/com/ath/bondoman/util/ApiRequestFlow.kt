package com.ath.bondoman.util

import com.ath.bondoman.model.dto.ApiResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withTimeoutOrNull
import retrofit2.Response

fun<T> apiRequestFlow(call: suspend () -> Response<T>): Flow<ApiResponse<T>> = flow {
    emit(ApiResponse.Loading)

    withTimeoutOrNull(20000L) {
        val response = call()

        try {
            if (response.isSuccessful) {
                response.body()?.let { data ->
                    emit(ApiResponse.Success(data))
                }
            } else {
                response.errorBody()?.let { error ->
                    error.close()
                    val message = when(response.code()) {
                        401 -> "Invalid email/password"
                        else -> "[${response.code()}] ${response.message()}"
                    }
                    emit(ApiResponse.Failure(message))
                }
            }
        } catch (e: Exception) {
            emit(ApiResponse.Failure(e.message ?: e.toString()))
        }
    } ?: emit(ApiResponse.Failure("Request timed out"))
}.flowOn(Dispatchers.IO)