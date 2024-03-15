package com.ath.bondoman.service

import com.ath.bondoman.api.AuthClient
import com.ath.bondoman.model.dto.LoginPayload
import com.ath.bondoman.util.apiRequestFlow
import javax.inject.Inject

class AuthService @Inject constructor(
    private val auth: AuthClient,
) {
    fun login(payload: LoginPayload) = apiRequestFlow {
        auth.login(payload)
    }
}