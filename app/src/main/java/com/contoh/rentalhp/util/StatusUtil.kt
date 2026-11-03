package com.contoh.rentalhp.util

import android.content.Context
import android.content.res.ColorStateList
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import com.contoh.rentalhp.R
import com.google.android.material.chip.Chip
import java.util.Locale

object StatusUtil {
    data class Style(
        @param:StringRes val label: Int,
        @param:ColorRes val background: Int,
        @param:ColorRes val foreground: Int
    )

    fun apply(view: TextView, status: String) {
        val style = getStyle(status)
        view.setText(style.label)
        val background = ColorStateList.valueOf(
            ContextCompat.getColor(view.context, style.background)
        )
        if (view is Chip) view.chipBackgroundColor = background else view.backgroundTintList = background
        view.setTextColor(ContextCompat.getColor(view.context, style.foreground))
    }

    fun label(context: Context, status: String): String = context.getString(getStyle(status).label)

    fun applyReturnTiming(view: TextView, lateDays: Int) {
        apply(view, if (lateDays > 0) "diajukan" else "tersedia")
        view.text = if (lateDays > 0) {
            view.resources.getQuantityString(R.plurals.late_days, lateDays, lateDays)
        } else {
            view.context.getString(R.string.on_time)
        }
    }

    private fun getStyle(status: String): Style {
        return when (status.lowercase(Locale.ROOT)) {
            "diajukan" -> Style(R.string.status_requested, R.color.status_requested_background, R.color.status_requested_text)
            "disetujui" -> Style(R.string.status_approved, R.color.status_approved_background, R.color.status_approved_text)
            "ditolak" -> Style(R.string.status_rejected, R.color.status_rejected_background, R.color.status_rejected_text)
            "dipinjam" -> Style(R.string.status_borrowed, R.color.status_borrowed_background, R.color.status_borrowed_text)
            "selesai" -> Style(R.string.status_completed, R.color.status_completed_background, R.color.status_completed_text)
            "tersedia" -> Style(R.string.status_available, R.color.status_approved_background, R.color.status_approved_text)
            "tidak_tersedia" -> Style(R.string.status_unavailable, R.color.status_rejected_background, R.color.status_rejected_text)
            "baik" -> Style(R.string.condition_good, R.color.status_approved_background, R.color.status_approved_text)
            "rusak_ringan" -> Style(R.string.condition_light_damage, R.color.status_requested_background, R.color.status_requested_text)
            "rusak_berat" -> Style(R.string.condition_heavy_damage, R.color.status_rejected_background, R.color.status_rejected_text)
            "hilang" -> Style(R.string.condition_lost, R.color.status_rejected_background, R.color.status_rejected_text)
            "admin" -> Style(R.string.role_admin, R.color.status_completed_background, R.color.status_completed_text)
            "petugas" -> Style(R.string.role_staff, R.color.status_completed_background, R.color.status_completed_text)
            "peminjam" -> Style(R.string.role_borrower, R.color.status_completed_background, R.color.status_completed_text)
            else -> Style(R.string.status_unknown, R.color.status_completed_background, R.color.status_completed_text)
        }
    }
}
