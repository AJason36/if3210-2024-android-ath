package com.ath.bondoman.api

import com.ath.bondoman.model.dto.LoginPayload
import com.ath.bondoman.model.dto.LoginResponse
import com.ath.bondoman.model.dto.TokenResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface AuthClient {
    @POST("auth/login")
    suspend fun login(
        @Body auth: LoginPayload,
    ): Response<LoginResponse>

    @POST("auth/token")
    suspend fun verifyJwt(@Header("Authorization") token: String): Response <TokenResponse>
}