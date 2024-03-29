package com.ath.bondoman.ui.settings

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.ath.bondoman.VerifyJwtService
import com.ath.bondoman.databinding.FragmentSettingsBinding
import com.ath.bondoman.model.Transaction
import com.ath.bondoman.repository.TokenRepository
import com.ath.bondoman.repository.TransactionRepository
import com.ath.bondoman.viewmodel.TokenViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject


@AndroidEntryPoint
class SettingsFragment : Fragment() {
    @Inject lateinit var transactionRepository: TransactionRepository

    private var _binding: FragmentSettingsBinding? = null
    private val tokenViewModel: TokenViewModel by activityViewModels()

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        val logoutButton = binding.logoutButton
        logoutButton.setOnClickListener {
            tokenViewModel.removeToken()
            val serviceIntent = Intent(requireActivity(), VerifyJwtService::class.java)
            requireActivity().stopService(serviceIntent)
        }

        tokenViewModel.token.observe(viewLifecycleOwner) { token ->
            token?.let {
                binding.emailText.text = token.email
            }
        }


        val sendMailButton = binding.sendEmailButton
        sendMailButton.setOnClickListener{
            lifecycleScope.launch {
                transactionRepository.getAll().collect{
                    transactions -> sendEmail(transactions)
                }
            }
        }
        return root
    }

    private fun sendEmail(transactions: List<Transaction>) {
//        ExcelUtil.writeToExcel(transactions, getFilesDir() + "/" + FILE_PATH)
//        val file: File = File(getFilesDir(), FILE_PATH)
//        val uri = Uri.fromFile(file)
        val emailIntent = Intent(Intent.ACTION_SEND)
        emailIntent.setType("vnd.android.cursor.dir/email")
        emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(tokenViewModel.token.value?.email))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Transaction List")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Transaction list from BondoMan is attached to this email")
        startActivity(Intent.createChooser(emailIntent, "Send email..."))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}