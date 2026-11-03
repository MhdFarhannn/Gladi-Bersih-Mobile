package com.contoh.rentalhp.data.model

data class ApiResponse<T>(
    val success: Boolean,
    val message: String,
    val data: T?
)

data class PagedData<T>(
    val items: List<T>,
    val page: Int,
    val pageSize: Int,
    val total: Long
)

data class ApiErrorBody(
    val success: Boolean = false,
    val message: String? = null
)
