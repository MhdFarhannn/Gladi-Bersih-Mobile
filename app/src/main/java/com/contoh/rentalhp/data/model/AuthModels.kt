package com.contoh.rentalhp.data.model

data class LoginRequest(
    val username: String,
    val password: String
)

data class RegisterRequest(
    val username: String,
    val nama: String,
    val password: String,
    val noTelepon: String,
    val alamat: String?
)

data class AuthResponse(
    val token: String,
    val expiresAt: String,
    val user: UserResponse
)

data class UserResponse(
    val id: Long,
    val username: String,
    val nama: String,
    val noTelepon: String,
    val alamat: String?,
    val role: String,
    val createdAt: String
)

data class UserCreateRequest(
    val username: String,
    val nama: String,
    val password: String,
    val noTelepon: String,
    val alamat: String?,
    val role: String
)

data class UserUpdateRequest(
    val username: String,
    val nama: String,
    val password: String?,
    val noTelepon: String,
    val alamat: String?,
    val role: String
)
