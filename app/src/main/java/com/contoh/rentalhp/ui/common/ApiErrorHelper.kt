package com.contoh.rentalhp.ui.common

import android.content.Context
import com.contoh.rentalhp.R
import com.contoh.rentalhp.data.model.ApiErrorBody
import com.google.gson.Gson
import retrofit2.HttpException
import java.io.IOException
import kotlinx.coroutines.CancellationException

object ApiErrorHelper {
    private lateinit var applicationContext: Context

    fun initialize(context: Context) {
        applicationContext = context.applicationContext
    }

    fun getMessage(error: Throwable): String {
        if (error is CancellationException) throw error
        return when (error) {
            is HttpException -> getHttpMessage(error)
            is UserMessageException -> error.message.orEmpty()
            is IOException -> applicationContext.getString(R.string.cannot_connect_server)
            else -> applicationContext.getString(R.string.general_error)
        }
    }

    private fun getHttpMessage(error: HttpException): String {
        if (error.code() == 401) return applicationContext.getString(R.string.session_expired)
        if (error.code() == 403) return applicationContext.getString(R.string.no_access)

        val backendMessage = runCatching {
            Gson().fromJson(error.response()?.errorBody()?.string(), ApiErrorBody::class.java).message
        }.getOrNull()
        return backendMessage?.takeIf { it.isNotBlank() }
            ?: applicationContext.getString(R.string.request_failed, error.code())
    }
}
