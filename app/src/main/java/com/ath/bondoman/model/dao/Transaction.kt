package com.ath.bondoman.model.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.ath.bondoman.model.Transaction

@Dao
interface TransactionDao {
    @Query("SELECT * FROM transactions")
    fun getAll(): LiveData<List<Transaction>>

    @Insert
    suspend fun insert(transaction: Transaction) : Long

    @Update
    suspend fun update(transaction: Transaction) : Int

    @Delete
    suspend fun delete(transaction: Transaction)
}