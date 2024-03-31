package com.ath.bondoman

import NetworkChangeReceiver
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.ath.bondoman.databinding.ActivityMainBinding
import com.ath.bondoman.viewmodel.TokenViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val tokenViewModel: TokenViewModel by viewModels()
    private lateinit var networkReceiver: NetworkChangeReceiver

    private fun checkToken() {
        tokenViewModel.token.observe(this)  { token ->
            if (token == null) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                startService(Intent(applicationContext, VerifyJwtService::class.java))
                initializeMainView()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        registerNetworkReceiver()
        checkToken()
    }

    private fun initializeMainView() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navView: BottomNavigationView = binding.navView

        val navHostFragment = supportFragmentManager.findFragmentById(R.id.nav_host_fragment_activity_main) as NavHostFragment
        val navController = navHostFragment.navController

        navView.setupWithNavController(navController)
    }


    private fun registerNetworkReceiver() {
        networkReceiver = NetworkChangeReceiver(this)
        val intentFilter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(networkReceiver, intentFilter)
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkReceiver)
    }
}