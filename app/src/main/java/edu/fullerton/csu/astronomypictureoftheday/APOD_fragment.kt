package edu.fullerton.csu.astronomypictureoftheday

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import edu.fullerton.csu.astronomypictureoftheday.databinding.FragmentApodBinding
import java.util.Calendar

private const val TAG = "APOD_fragment"

class APOD_fragment : Fragment() {

    private val dateViewModel: APOD_ViewModel by viewModels()

    // create binding for xml file -> binding class made automatically by enabling ViewBinding
    private lateinit var binding: FragmentApodBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ignore default boilerplate below
        // return super.onCreateView(inflater, container, savedInstanceState)
        binding = FragmentApodBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {

            btnPrev.setOnClickListener {
                dateViewModel.decrementDate()
                Log.d(TAG, "Decremented date by 1: ${dateViewModel.currentDate.time}")
                // update picture and description,author,etc from NASA according to
                // dateViewModel.currentDate.get(Calendar.YEAR)
                // dateViewModel.currentDate.get(Calendar.MONTH)
                // dateViewModel.currentDate.get(Calendar.DAY_OF_MONTH)
                // call updateAPOD()
            }

            btnNext.setOnClickListener {
                dateViewModel.incrementDate()
                Log.d(TAG, "Incremented date by 1: ${dateViewModel.currentDate.time}")
                // update picture and description,author,etc from NASA
            }

            btnDatePicker.setOnClickListener {
                // get date from date selected by user
                // set the date with -> dateViewModel.setDate(year, month, day)

                // hard-coded values to test setDate functionality
                // this is the first day an APOD was posted by NASA
                // june 16, 1995
                val year = 1995
                val month = 5 // I think values are from 0 to 11. 5 is june
                val day = 16

                dateViewModel.setDate(year, month, day)
                Log.d(TAG, "Date set by user: ${dateViewModel.currentDate.time}")
                // update picture and description,author,etc from NASA with new date
            }
        }
    }

    // function signature can be changed
    fun updateAPOD(year: Int, month: Int, day: Int) {
        // update ImageView and Text from NASA's api using the date passed in
    }

}