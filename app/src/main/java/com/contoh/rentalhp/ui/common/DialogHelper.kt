package com.contoh.rentalhp.ui.common

import android.content.Context
import com.contoh.rentalhp.R
import com.google.android.material.dialog.MaterialAlertDialogBuilder

object DialogHelper {
    fun confirm(
        context: Context,
        title: String,
        message: String,
        positiveText: String,
        onConfirmed: () -> Unit
    ) {
        MaterialAlertDialogBuilder(context)
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton(context.getString(R.string.cancel), null)
            .setPositiveButton(positiveText) { _, _ -> onConfirmed() }
            .show()
    }
}
