package com.ath.bondoman.repository

import androidx.lifecycle.LiveData
import com.ath.bondoman.model.Transaction
import com.ath.bondoman.model.dao.TransactionDao
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class TransactionRepository @Inject constructor(private val transactionDao: TransactionDao) {
    val allTransactions: LiveData<List<Transaction>> = transactionDao.getAll()

    suspend fun insert(transaction: Transaction) : Long {
        return transactionDao.insert(transaction)
    }

    suspend fun update(transaction: Transaction) : Int {
        return transactionDao.update(transaction)
    }

    suspend fun delete(transaction: Transaction) {
        transactionDao.delete(transaction)
    }
}
