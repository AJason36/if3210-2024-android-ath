package com.ath.bondoman.api

import com.ath.bondoman.model.dto.LoginPayload
import com.ath.bondoman.model.dto.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthClient {
    @POST("auth/login")
    suspend fun login(
        @Body auth: LoginPayload,
    ): Response<LoginResponse>
}