package com.ath.bondoman.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.ath.bondoman.LoginActivity
import com.ath.bondoman.databinding.FragmentSettingsBinding
import com.ath.bondoman.viewmodel.SettingsViewModel
import com.ath.bondoman.viewmodel.TokenViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsFragment  : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val tokenViewModel: TokenViewModel by viewModels()
    private val settingsViewModel: SettingsViewModel by viewModels()

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

            startActivity(Intent(requireContext(), LoginActivity::class.java))
            requireActivity().finish()
        }
        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}