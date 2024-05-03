package edu.fullerton.csu.astronomypictureoftheday

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.util.Calendar

class CalendarDatePicker: DialogFragment() {
    override fun onCreate(savedInstanceState: Bundle?): DatePickerDialog {
        super.onCreate(savedInstanceState)

        val calendar = Calendar.getInstance()
        val initialYear = calendar.get(Calendar.YEAR)
        val initialMonth = calendar.get(Calendar.MONTH)
        val initialDay = calendar.get(Calendar.DAY_OF_MONTH)

        return DatePickerDialog(
            requireContext(),
            null,
            initialYear,
            initialMonth,
            initialDay)
    }
}