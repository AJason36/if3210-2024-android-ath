package com.ath.bondoman

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.ath.bondoman.api.AuthClient
import com.ath.bondoman.model.dto.ApiResponse
import com.ath.bondoman.repository.TokenRepository
import com.ath.bondoman.util.apiRequestFlow
import com.ath.bondoman.util.isNetworkAvailable
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@AndroidEntryPoint
class VerifyJwtService: Service() {
    @Inject
    lateinit var authClient: AuthClient
    @Inject
    lateinit var tokenRepository: TokenRepository
    private lateinit var handler: Handler
    private lateinit var runnable: Runnable
    private lateinit var job: Job
    private val tag = "BACKGROUND SERVICE"

    override fun onCreate() {
        super.onCreate()
        handler = Handler(Looper.getMainLooper())
        job = Job()
        runnable = Runnable {
            verifyJwt()
        }
        handler.postDelayed(runnable, 0)
    }

    private fun verifyJwt() {
        Log.d(this.tag, "Starting check token request")
        val isConnected = isNetworkAvailable(this)
        if (isConnected) {
            CoroutineScope(Dispatchers.IO + job).launch {
                val token = tokenRepository.getToken()
                if (token != null) {
                    fetchTokenResult(token.token)
                }
            }
        } else {
            handler.postDelayed(runnable, 2 * 60 * 1000)
        }
    }

    private suspend fun fetchTokenResult(token: String) {
        apiRequestFlow { authClient.verifyJwt("Bearer $token") }.collect { response ->
            when (response) {
                is ApiResponse.Failure -> handleFailure()
                // check again 2 mins later if it s not expired yet
                is ApiResponse.Success -> handler.postDelayed(runnable, 2 * 60 * 1000)
                else -> {}
            }
        }
    }

    private suspend fun handleFailure() {
        tokenRepository.removeToken()
        withContext(Dispatchers.Main) {
            val intent = Intent(applicationContext, LoginActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
            }
            applicationContext.startActivity(intent)
        }
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(runnable)
    }
}
