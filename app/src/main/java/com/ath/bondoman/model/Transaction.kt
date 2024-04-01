package com.ath.bondoman.model

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.parcelize.Parcelize

enum class TransactionCategory {
    Income, Expenditure
}

class TransactionCategoryConverter {
    @TypeConverter
    fun toCategory(value: String) = enumValueOf<TransactionCategory>(value)

    @TypeConverter
    fun fromCategory(value: TransactionCategory) = value.name
}

@Entity(tableName = "transactions")
@Parcelize
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val category: TransactionCategory,
    val amount: Double,
    val location: LocationData?,
    val date: String = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
    val userEmail: String
): Parcelable


