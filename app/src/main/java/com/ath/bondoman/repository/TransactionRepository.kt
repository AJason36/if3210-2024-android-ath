package com.ath.bondoman.repository

import com.ath.bondoman.model.Transaction
import com.ath.bondoman.model.dao.TransactionDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepository @Inject constructor(private val transactionDao: TransactionDao) {
    fun getAll(): Flow<List<Transaction>> {
        return transactionDao.getAll()
    }

    fun getAllIncome(): Flow<Double>{
        return transactionDao.getAllIncome()
    }

    fun getAllExpenditure(): Flow<Double>{
        return transactionDao.getAllExpenditure()
    }

    suspend fun insert(transaction: Transaction) : Long {
        return transactionDao.insert(transaction)
    }

    suspend fun update(transaction: Transaction) : Int {
        return transactionDao.update(transaction)
    }

    suspend fun delete(transaction: Transaction) : Int {
        return transactionDao.delete(transaction)
    }
}
