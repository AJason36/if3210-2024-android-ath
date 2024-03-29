package com.ath.bondoman.ui.transaction

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.lifecycle.LiveData
import androidx.recyclerview.widget.RecyclerView
import com.ath.bondoman.R
import com.ath.bondoman.databinding.TransactionListItemBinding
import com.ath.bondoman.model.Transaction

class TransactionListAdapter : RecyclerView.Adapter<TransactionListAdapter.TransactionViewHolder>() {

    private var transactions: List<Transaction> = listOf()

    class TransactionViewHolder(private val binding: TransactionListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val date: TextView = binding.date
        val category: TextView = binding.category
        val title: TextView = binding.title
        val amount: TextView = binding.amount
        val location: TextView = binding.location
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TransactionListItemBinding.inflate(inflater, parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.date.text = transaction.date
        holder.category.text = transaction.category.toString()
        holder.title.text = transaction.title
        holder.amount.text = transaction.amount.toString()
        holder.location.text = transaction.location?.address
    }

    override fun getItemCount() = transactions.size

    fun setTransactions(transactions: List<Transaction>) {
        Log.d("TransactionViewModel", "Transactions2: $transactions")
        this.transactions = transactions
        Log.d("TransactionViewModel", "Transactions3: ${this.transactions}")
        notifyDataSetChanged()
    }
}
