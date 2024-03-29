package com.ath.bondoman.ui.transaction

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ath.bondoman.AddTransactionActivity
import com.ath.bondoman.databinding.FragmentTransactionBinding
import com.ath.bondoman.model.Transaction
import com.ath.bondoman.viewmodel.TransactionViewModel

class TransactionFragment : Fragment() {

    private var _binding: FragmentTransactionBinding? = null

    private val binding get() = _binding!!

//    private lateinit var transactionViewModel: TransactionViewModel
//    private lateinit var transactionAdapter: TransactionAdapter

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
//        val transactionViewModel =
//                ViewModelProvider(this).get(TransactionViewModel::class.java)

        _binding = FragmentTransactionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Add Transaction Button
        val addTransactionBtn = binding.addTransactionBtn
        addTransactionBtn.setOnClickListener {
            val intent = Intent(context, AddTransactionActivity::class.java)
            startActivity(intent)
        }

//        val textView: TextView = binding.textTransaction
//        transactionViewModel.text.observe(viewLifecycleOwner) {
//            textView.text = it
//        }

        // Initialize the RecyclerView and its adapter
//        val recyclerView: RecyclerView = binding.recyclerView
//        transactionAdapter = TransactionAdapter(listOf())
//        recyclerView.adapter = transactionAdapter
//        recyclerView.layoutManager = LinearLayoutManager(context)
//
//        // Observe the transactions LiveData
//        transactionViewModel.transactions.observe(viewLifecycleOwner, Observer { transactions: Transaction ->
//            // Update the RecyclerView's data
//            transactionAdapter.transactions = transactions
//            transactionAdapter.notifyDataSetChanged()
//        })

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}