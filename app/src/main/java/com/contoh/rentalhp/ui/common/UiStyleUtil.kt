package com.contoh.rentalhp.ui.common

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.color.MaterialColors
import com.google.android.material.R

object UiStyleUtil {
    fun styleRefresh(view: SwipeRefreshLayout) {
        view.setColorSchemeColors(MaterialColors.getColor(view, androidx.appcompat.R.attr.colorPrimary))
        view.setProgressBackgroundColorSchemeColor(MaterialColors.getColor(view, R.attr.colorSurface))
    }
}
