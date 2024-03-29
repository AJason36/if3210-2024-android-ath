package com.ath.bondoman.model.dto

data class TokenResponse (
    val nim: String,
    val iat: Int,
    val exp: Int
)