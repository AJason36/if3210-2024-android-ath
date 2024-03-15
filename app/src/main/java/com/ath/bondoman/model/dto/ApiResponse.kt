package com.ath.bondoman.model.dto

sealed class ApiResponse<out T> {
    object Loading: ApiResponse<Nothing>()

    data class Success<out T>(
        val data: T
    ): ApiResponse<T>()

    data class Failure(
        val message: String,
    ): ApiResponse<Nothing>()
}