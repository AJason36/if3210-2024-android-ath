package com.ath.bondoman.model.dto

data class UploadResponse(
    val items: ItemsList
)

data class ItemsList(
    val items: List<Item>
)

data class Item(
    val name: String,
    val qty: Int,
    val price: Double
)