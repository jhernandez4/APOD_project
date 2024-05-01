package edu.fullerton.csu.astronomypictureoftheday

import android.util.Log
import androidx.lifecycle.ViewModel
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

private const val TAG = "APOD_ViewModel"
class APOD_ViewModel : ViewModel() {
    init {
        Log.d(TAG, "ViewModel instance created")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel instance about to be destroyed")
    }

    private var currentDateCalendar: Calendar = GregorianCalendar()

    fun incrementDate() {
        currentDateCalendar.add(Calendar.DAY_OF_MONTH, 1)
    }

    fun decrementDate() {
        currentDateCalendar.add(Calendar.DAY_OF_MONTH, -1)
    }

    fun setDate(myCalendar: Calendar){
        currentDateCalendar.set(Calendar.YEAR, myCalendar.get(Calendar.YEAR))
        currentDateCalendar.set(Calendar.MONTH, myCalendar.get(Calendar.MONTH))
        currentDateCalendar.set(Calendar.DAY_OF_MONTH, myCalendar.get(Calendar.DAY_OF_MONTH))
    }
}