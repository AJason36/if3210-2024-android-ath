package com.ath.bondoman

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ath.bondoman.databinding.ActivityLoginBinding
import com.ath.bondoman.model.dto.ApiResponse
import com.ath.bondoman.model.dto.LoginPayload
import com.ath.bondoman.viewmodel.AuthViewModel
import com.ath.bondoman.viewmodel.CoroutinesErrorHandler
import com.ath.bondoman.viewmodel.TokenViewModel
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {
    private val authViewModel: AuthViewModel by viewModels()
    private val tokenViewModel: TokenViewModel by viewModels()
    private lateinit var binding: ActivityLoginBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        checkToken()

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        findViewById<Button>(R.id.login_button).setOnClickListener {
            val emailEt = findViewById<EditText>(R.id.email_edit_text)
            val passwordEt = findViewById<EditText>(R.id.password_edit_text)
            val payload = LoginPayload(emailEt.text.toString(), passwordEt.text.toString())
            authViewModel.login(payload, object: CoroutinesErrorHandler {
                override fun onError(message: String) {
                    Snackbar.make(findViewById(R.id.container), message, Snackbar.LENGTH_SHORT).show()
                }
            })
        }

        authViewModel.loginResponse.observe(this) { response ->
            val msg = when (response) {
                is ApiResponse.Failure -> response.message
                ApiResponse.Loading -> "loading"
                is ApiResponse.Success -> {
                    val token = response.data.token
                    tokenViewModel.saveToken(token)
                    token
                }
                else -> "Unknown response"
            }

            Snackbar.make(findViewById(R.id.container), msg, Snackbar.LENGTH_SHORT).show()
        }
    }
    private fun checkToken() {
        tokenViewModel.token.observe(this)  { token ->
            if (token != null) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }
    }
}