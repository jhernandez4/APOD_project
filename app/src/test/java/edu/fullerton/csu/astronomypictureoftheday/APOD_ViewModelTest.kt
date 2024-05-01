package edu.fullerton.csu.astronomypictureoftheday

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar

class APOD_ViewModelTest{
    @Test
    fun addsDayToCalendar(){
        var savedStatehandle = SavedStateHandle()
        var apodViewModel = APOD_ViewModel(savedStatehandle)

        val initialDate = apodViewModel.currentDate.clone() as Calendar

        // Expected date is the current day plus one
        val expectedDate = initialDate.clone() as Calendar
        expectedDate.add(Calendar.DAY_OF_MONTH, 1)

        // Call the method to be tested
        apodViewModel.incrementDate()

        // Assert that the currentDate has been incremented correctly
        assertEquals(
            expectedDate.get(Calendar.DAY_OF_MONTH),
            apodViewModel.currentDate.get(Calendar.DAY_OF_MONTH)
        )
    }

    @Test
    fun decrementsDayToCalendar(){
        var savedStatehandle = SavedStateHandle()
        var apodViewModel = APOD_ViewModel(savedStatehandle)

        val initialDate = apodViewModel.currentDate.clone() as Calendar

        // Expected date is the current day plus one
        val expectedDate = initialDate.clone() as Calendar
        expectedDate.add(Calendar.DAY_OF_MONTH, -1)

        // Call the method to be tested
        apodViewModel.decrementDate()

        assertEquals(
            expectedDate.get(Calendar.DAY_OF_MONTH),
            apodViewModel.currentDate.get(Calendar.DAY_OF_MONTH)
        )
    }
}