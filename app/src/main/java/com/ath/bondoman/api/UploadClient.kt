package com.ath.bondoman.api

import com.ath.bondoman.model.dto.UploadResponse
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface UploadClient {
    @Multipart
    @POST("bill/upload")
    suspend fun upload(
        @Header("Authorization") token: String,
        @Part file: MultipartBody.Part,
    ): Response<UploadResponse>
}