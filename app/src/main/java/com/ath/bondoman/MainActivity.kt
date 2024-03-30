package com.ath.bondoman

import NetworkChangeReceiver
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.ath.bondoman.databinding.ActivityMainBinding
import com.ath.bondoman.ui.transaction.TransactionFormFragment
import com.ath.bondoman.viewmodel.TokenViewModel
import com.google.android.material.bottomnavigation.BottomNavigationView
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    companion object {
        const val TRANSACTION_FORM_BROADCAST_RECEIVER = "TRANSACTION_FORM_BROADCAST_RECEIVER"
    }

    private lateinit var binding: ActivityMainBinding
    private val tokenViewModel: TokenViewModel by viewModels()
    private lateinit var networkReceiver: NetworkChangeReceiver
//    private lateinit var broadcastReceiver: BroadcastReceiver

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
//        registerBroadcastReceiver()
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

//    private fun registerBroadcastReceiver() {
//        broadcastReceiver = object : BroadcastReceiver() {
//            override fun onReceive(context: Context, intent: Intent) {
//                val randomAmount = intent.getIntExtra("random_amount", 0)
//                // Open TransactionFormFragment with the received random amount value
//                val transactionFormFragment = TransactionFormFragment().apply {
//                    arguments = Bundle().apply {
//                        putInt("random_amount", randomAmount)
//                    }
//                }
//                supportFragmentManager.beginTransaction()
//                    .replace(R.id.nav_host_fragment_activity_main, transactionFormFragment)
//                    .commit()
//            }
//        }
//
//        val intentFilter = IntentFilter(TRANSACTION_FORM_BROADCAST_RECEIVER)
//        registerReceiver(broadcastReceiver, intentFilter)
//    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(networkReceiver)
//        unregisterReceiver(broadcastReceiver)
    }
}