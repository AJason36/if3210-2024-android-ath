package com.ath.bondoman.model.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ath.bondoman.model.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions ORDER BY date DESC")
    fun getAll(): Flow<List<Transaction>>

    @Query("SELECT SUM(amount) FROM transactions WHERE category = 'Income'")
    fun getAllIncome():Flow<Double>

    @Query("SELECT SUM(amount) FROM transactions WHERE category = 'Expenditure'")
    fun getAllExpenditure():Flow<Double>
    @Insert
    suspend fun insert(transaction: Transaction) : Long

    @Update
    suspend fun update(transaction: Transaction) : Int

    @Delete
    suspend fun delete(transaction: Transaction) : Int
}