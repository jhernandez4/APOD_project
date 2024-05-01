package edu.fullerton.csu.astronomypictureoftheday

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar

private const val TAG = "APOD_ViewModel"
const val CURRENT_INDEX_KEY = "CURRENT_INDEX_KEY"

class APOD_ViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {

//    init {
//        Log.d(TAG, "ViewModel instance created")
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        Log.d(TAG, "ViewModel instance about to be destroyed")
//    }

    private var currentDateCalendar: Calendar
        // Retrieve the saved value or create a new Calendar instance if not available
        get() = savedStateHandle.get(CURRENT_INDEX_KEY) ?: GregorianCalendar().apply {
            savedStateHandle.set(CURRENT_INDEX_KEY, this)
        }
        // Update the saved value whenever currentDateCalendar is modified
        set(value) {
            savedStateHandle.set(CURRENT_INDEX_KEY, value)
        }

    val currentDate: Calendar
        get() = currentDateCalendar

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