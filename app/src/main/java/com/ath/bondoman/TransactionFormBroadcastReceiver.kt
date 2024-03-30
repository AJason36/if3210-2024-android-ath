package com.ath.bondoman

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class TransactionFormBroadcastReceiver : BroadcastReceiver() {
    companion object {
        const val TRANSACTION_FORM_BROADCAST_RECEIVER = "TRANSACTION_FORM_BROADCAST_RECEIVER"
        const val RECEIVE_RANDOM_AMOUNT = "RECEIVE_RANDOM_AMOUNT"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val randomAmount = intent.getLongExtra(RECEIVE_RANDOM_AMOUNT, 0)
        val transactionFormIntent = Intent(context, TransactionFormActivity::class.java).apply {
            putExtra(TransactionFormActivity.EXTRA_RANDOM_AMOUNT, randomAmount)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(transactionFormIntent)
    }
}
