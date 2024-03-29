package com.ath.bondoman.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class TransactionCategory {
    Income, Expenditure
}

@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: TransactionCategory,
    val amount: Double,
    val location: String,
    val date: String = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
)

class TransactionCategoryConverter {
    @TypeConverter
    fun toCategory(value: String) = enumValueOf<TransactionCategory>(value)

    @TypeConverter
    fun fromCategory(value: TransactionCategory) = value.name
}
