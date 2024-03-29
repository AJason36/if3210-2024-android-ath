package com.ath.bondoman.model.dto

import com.ath.bondoman.model.TransactionCategory

data class TransactionDTO(
    val title: String,
    val category: TransactionCategory,
    val amount: Double,
    val location: String
)
