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
    @Query("SELECT * FROM transactions WHERE userEmail = :userEmail ORDER BY date DESC")
    fun getAll(userEmail: String): Flow<List<Transaction>>

    @Query("SELECT SUM(amount) FROM transactions WHERE category = 'Income' AND userEmail = :userEmail")
    fun getAllIncome(userEmail: String):Flow<Double>

    @Query("SELECT SUM(amount) FROM transactions WHERE category = 'Expenditure' AND userEmail = :userEmail")
    fun getAllExpenditure(userEmail: String):Flow<Double>
    @Insert
    suspend fun insert(transaction: Transaction) : Long

    @Update
    suspend fun update(transaction: Transaction) : Int

    @Delete
    suspend fun delete(transaction: Transaction) : Int
}