package com.contoh.rentalhp.ui.common

data class ListRow<T>(
    val id: Long,
    val item: T,
    val title: String,
    val subtitle: String? = null,
    val meta: String? = null,
    val status: String? = null,
    val imageUrl: String? = null,
    val showImage: Boolean = false,
    val primaryAction: String? = null,
    val secondaryAction: String? = null,
    val clickable: Boolean = true,
    val supportingText: String? = null
)
