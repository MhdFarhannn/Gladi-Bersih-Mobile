package com.contoh.rentalhp.ui.common

import androidx.fragment.app.FragmentManager
import com.contoh.rentalhp.R
import com.contoh.rentalhp.util.FormatUtil
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.DateValidatorPointBackward
import java.time.LocalDate
import java.time.ZoneOffset

object DatePickerHelper {
    fun show(
        fragmentManager: FragmentManager,
        title: String,
        initialDate: String?,
        maxToday: Boolean = false,
        onSelected: (String) -> Unit
    ) {
        val requestedSelection = runCatching {
            LocalDate.parse(initialDate).atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
        }.getOrDefault(MaterialDatePicker.todayInUtcMilliseconds())
        val selection = if (maxToday) {
            minOf(requestedSelection, MaterialDatePicker.todayInUtcMilliseconds())
        } else {
            requestedSelection
        }
        val builder = MaterialDatePicker.Builder.datePicker()
            .setTitleText(title)
            .setPositiveButtonText(R.string.select_date)
            .setNegativeButtonText(R.string.cancel)
            .setSelection(selection)
        if (maxToday) {
            builder.setCalendarConstraints(
                CalendarConstraints.Builder()
                    .setValidator(DateValidatorPointBackward.now())
                    .build()
            )
        }
        val picker = builder.build()
        picker.addOnPositiveButtonClickListener { onSelected(FormatUtil.toApiDate(it)) }
        picker.show(fragmentManager, "date_picker")
    }
}
