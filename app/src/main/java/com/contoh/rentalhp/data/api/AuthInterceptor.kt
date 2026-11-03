package com.contoh.rentalhp.data.api

import com.contoh.rentalhp.util.SessionManager
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionManager: SessionManager
) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val token = sessionManager.token
        val request = if (token.isNullOrBlank()) {
            originalRequest
        } else {
            originalRequest.newBuilder()
                .header("Authorization", "Bearer $token")
                .build()
        }

        val response = chain.proceed(request)
        val isLoginRequest = request.url.encodedPath == "/api/auth/login"
        if (response.code == 401 && !token.isNullOrBlank() && !isLoginRequest) {
            sessionManager.clearSession()
            SessionEvents.notifyExpired()
        }
        return response
    }
}
