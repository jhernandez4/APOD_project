package edu.fullerton.csu.astronomypictureoftheday

import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.GregorianCalendar
import androidx.lifecycle.MutableLiveData


class APOD_ViewModelTest{
    @Test
    fun addsDayToCalendar(){
        var apodViewModel = createViewModel()

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
        var apodViewModel = createViewModel()

        val initialDate = apodViewModel.currentDate.clone() as Calendar

        // Expected date is the current day minus one
        val expectedDate = initialDate.clone() as Calendar
        expectedDate.add(Calendar.DAY_OF_MONTH, -1)

        // Call the method to be tested
        apodViewModel.decrementDate()

        assertEquals(
            expectedDate.get(Calendar.DAY_OF_MONTH),
            apodViewModel.currentDate.get(Calendar.DAY_OF_MONTH)
        )
    }

    @Test
    fun setSpecificDateToCalendar(){
        var apodViewModel = createViewModel()

        // Date of the first posted picture for APOD by NASA
        val year = 1995
        val month = 6
        val day = 16

        val initialDate = apodViewModel.currentDate.clone() as Calendar

        val expectedDate = initialDate.clone() as Calendar
        expectedDate.set(Calendar.YEAR, year)
        expectedDate.set(Calendar.MONTH, month)
        expectedDate.set(Calendar.DAY_OF_MONTH, day)

        apodViewModel.setDate(year, month, day)

        assert(expectedDate.equals(apodViewModel.currentDate))
    }

    fun createViewModel(): APOD_ViewModel {
        var savedStateHandle = SavedStateHandle()
        var apodViewModel = APOD_ViewModel(savedStateHandle)

        return apodViewModel
    }
}