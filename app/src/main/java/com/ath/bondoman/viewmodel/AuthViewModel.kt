package com.ath.bondoman.viewmodel

import androidx.lifecycle.MutableLiveData
import com.ath.bondoman.model.dto.ApiResponse
import com.ath.bondoman.model.dto.LoginPayload
import com.ath.bondoman.model.dto.LoginResponse
import com.ath.bondoman.service.AuthService
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authService: AuthService
): BaseViewModel() {

    private val _loginResponse = MutableLiveData<ApiResponse<LoginResponse>>()
    val loginResponse = _loginResponse

    fun login(payload: LoginPayload, coroutinesErrorHandler: CoroutinesErrorHandler) = baseRequest(
        _loginResponse,
        coroutinesErrorHandler
    ) {
        authService.login(payload)
    }
}