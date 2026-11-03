package com.contoh.rentalhp.data.api

import android.content.Context
import com.contoh.rentalhp.BuildConfig
import com.contoh.rentalhp.util.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    lateinit var apiService: ApiService
        private set

    fun initialize(context: Context) {
        if (::apiService.isInitialized) return

        val clientBuilder = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(SessionManager.getInstance(context)))
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        if (BuildConfig.DEBUG) {
            clientBuilder.addInterceptor(
                HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BASIC
                    redactHeader("Authorization")
                }
            )
        }

        apiService = Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(clientBuilder.build())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
