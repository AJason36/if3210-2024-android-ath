package com.ath.bondoman.ui.transaction

import android.content.Intent
import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.ath.bondoman.R
import com.ath.bondoman.TransactionFormActivity
import com.ath.bondoman.databinding.TransactionListItemBinding
import com.ath.bondoman.model.Transaction
import com.ath.bondoman.model.TransactionCategory
import com.ath.bondoman.util.formatDate
import com.ath.bondoman.util.NumberFormatUtils

class TransactionListAdapter : RecyclerView.Adapter<TransactionListAdapter.TransactionViewHolder>() {

    private var transactions: List<Transaction> = listOf()

    class TransactionViewHolder(private val binding: TransactionListItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val date: TextView = binding.date
        val category: TextView = binding.category
        val title: TextView = binding.title
        val amount: TextView = binding.amount
        val location: TextView = binding.location
        val categoryCircle: View = binding.categoryCircle
        val categoryRectangle: LinearLayout = binding.categoryRectangle
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = TransactionListItemBinding.inflate(inflater, parent, false)
        return TransactionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        val context = holder.itemView.context
        holder.date.text = formatDate(transaction.date)
        holder.category.text = transaction.category.toString()
        holder.title.text = transaction.title
        holder.amount.text = NumberFormatUtils.formatNumberString(transaction.amount.toString())
        holder.location.text = transaction.location?.address

        if (transaction.category == TransactionCategory.Expenditure) {
            holder.categoryCircle.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.rose_400))
            holder.categoryRectangle.setBackgroundColor(ContextCompat.getColor(context, R.color.rose_400))
        } else {
            holder.categoryCircle.backgroundTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green_400))
            holder.categoryRectangle.setBackgroundColor(ContextCompat.getColor(context, R.color.green_400))
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, TransactionFormActivity::class.java).apply {
                putExtra(TransactionFormActivity.EXTRA_TRANSACTION, transaction)
                putExtra(TransactionFormActivity.EXTRA_MODE, TransactionFormActivity.MODE_EDIT)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = transactions.size

    fun setTransactions(transactions: List<Transaction>) {
        this.transactions = transactions
        notifyDataSetChanged()
    }
}
