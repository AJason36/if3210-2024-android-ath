package com.ath.bondoman

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.ath.bondoman.databinding.ActivityLoginBinding
import com.ath.bondoman.model.Token
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
    }

    private fun checkToken() {
        tokenViewModel.token.observe(this)  { token ->
            if (token != null) {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            } else {
                initializeLoginView()
            }
        }
    }

    private fun initializeLoginView() {
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val loginButton = binding.loginButton
        val emailEt =  binding.emailEditText
        val passwordEt = binding.passwordEditText
        val emailErrorTextView = binding.emailErrorText
        val passwordErrorTextView = binding.passwordErrorText
        val loginErrorText = binding.loginErrorText

        loginButton.setOnClickListener {
            val email = emailEt.text.toString()
            val password = passwordEt.text.toString()
            val payload = LoginPayload(email, password)

            if (email.isEmpty()) {
                emailErrorTextView.visibility = View.VISIBLE
                emailErrorTextView.setText("Email is required")
            } else if (!isValidEmail(email)) {
                emailErrorTextView.visibility = View.VISIBLE
                emailErrorTextView.setText("Invalid email format")
            } else {
                emailErrorTextView.visibility = View.GONE
            }

            if (password.isEmpty()) {
                passwordErrorTextView.visibility = View.VISIBLE
            } else {
                passwordErrorTextView.visibility = View.GONE
            }

            if (email.isNotEmpty() && password.isNotEmpty() && isValidEmail(email)) {
                authViewModel.login(payload, object : CoroutinesErrorHandler {
                    override fun onError(message: String) {
                        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }

        authViewModel.loginResponse.observe(this) { response ->
            when (response) {
                is ApiResponse.Failure -> {
                    val errorMessage = when {
                        response.code == 400 || response.code == 401 -> "Invalid username or password"
                        response.message.contains("Unable to resolve host") -> "Device not connected to the internet"
                        else -> response.message
                    }
                    loginErrorText.text = errorMessage
                    loginErrorText.visibility = View.VISIBLE
                }

                is ApiResponse.Success -> {
                    loginErrorText.visibility = View.GONE
                    val token = Token(response.data.token, binding.emailEditText.text.toString())
                    tokenViewModel.saveToken(token)
                    Toast.makeText(applicationContext, "Login successful", Toast.LENGTH_SHORT).show()
                }

                else -> {
                }
            }
        }

    }

    private fun isValidEmail(email: String): Boolean {
        val emailRegex = ("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,}\$").toRegex()
        return emailRegex.matches(email)
    }
}