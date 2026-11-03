package com.contoh.rentalhp.data.api

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow

object SessionEvents {
    private val mutableExpired = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val expired = mutableExpired.asSharedFlow()

    fun notifyExpired() {
        mutableExpired.tryEmit(Unit)
    }
}
