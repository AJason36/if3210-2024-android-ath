package com.ath.bondoman.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.ath.bondoman.model.LocationDataTypeConverter
import com.ath.bondoman.model.TransactionCategoryConverter
import com.ath.bondoman.model.Transaction
import com.ath.bondoman.model.dao.TransactionDao


@Database(entities = [Transaction::class], version = 1)
@TypeConverters(TransactionCategoryConverter::class, LocationDataTypeConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao
}