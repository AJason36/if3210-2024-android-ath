package com.ath.bondoman.ui.transaction

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.ath.bondoman.R
import com.ath.bondoman.databinding.FragmentTransactionBinding
import com.ath.bondoman.viewmodel.TransactionViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TransactionFragment : Fragment() {

    private var _binding: FragmentTransactionBinding? = null

    private val binding get() = _binding!!

    private val transactionViewModel: TransactionViewModel by viewModels()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTransactionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        // Add Transaction Button
        val addTransactionBtn = binding.addTransactionBtn
        addTransactionBtn.setOnClickListener {
            it.findNavController().navigate(R.id.action_transactionFragment_to_transactionFormFragment)
        }

        val adapter = TransactionListAdapter()
        val recyclerView: RecyclerView = binding.transactionList
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        transactionViewModel.allTransactions.observe(viewLifecycleOwner) { transactions ->
            transactions?.let { adapter.setTransactions(it) }
            Log.d("TransactionViewModel", "Transactions: $transactions")
        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}