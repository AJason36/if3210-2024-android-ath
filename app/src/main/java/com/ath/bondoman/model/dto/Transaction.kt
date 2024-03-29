package com.ath.bondoman.model.dto

import com.ath.bondoman.model.LocationData
import com.ath.bondoman.model.TransactionCategory

data class InsertTransactionDTO(
    val title: String,
    val category: TransactionCategory,
    val amount: Double,
    val location: LocationData?
)

data class UpdateTransactionDTO(
    val id: Long,
    val title: String,
    val category: TransactionCategory,
    val amount: Double,
    val location: LocationData?,
    val date: String
)
