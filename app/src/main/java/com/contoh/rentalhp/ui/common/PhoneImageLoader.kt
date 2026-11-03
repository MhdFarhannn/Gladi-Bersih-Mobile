package com.contoh.rentalhp.ui.common

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.contoh.rentalhp.BuildConfig
import com.contoh.rentalhp.R

object PhoneImageLoader {
    fun url(imagePath: String?): String? {
        val path = imagePath?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return if (path.startsWith("http://") || path.startsWith("https://")) {
            path
        } else {
            "${BuildConfig.BASE_URL.trimEnd('/')}/${path.trimStart('/')}"
        }
    }

    fun load(view: ImageView, imagePath: String?, name: String? = null) {
        view.contentDescription = if (name.isNullOrBlank()) {
            view.context.getString(R.string.phone_image)
        } else {
            view.context.getString(R.string.phone_image_named, name)
        }
        Glide.with(view)
            .load(url(imagePath))
            .placeholder(R.drawable.ic_phone_placeholder)
            .fallback(R.drawable.ic_phone_placeholder)
            .error(R.drawable.ic_phone_image_error)
            .fitCenter()
            .into(view)
    }
}
