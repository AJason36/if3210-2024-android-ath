package com.ath.bondoman.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ath.bondoman.TransactionFormActivity

class TransactionFormBroadcastReceiver : BroadcastReceiver() {
    companion object {
        const val TRANSACTION_FORM_BROADCAST_RECEIVER = "TRANSACTION_FORM_BROADCAST_RECEIVER"
        const val RECEIVE_RANDOM_AMOUNT = "RECEIVE_RANDOM_AMOUNT"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val randomAmount = intent.getDoubleExtra(RECEIVE_RANDOM_AMOUNT, 0.0)
        val transactionFormIntent = Intent(context, TransactionFormActivity::class.java).apply {
            putExtra(TransactionFormActivity.EXTRA_RANDOM_AMOUNT, randomAmount)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(transactionFormIntent)
    }
}
