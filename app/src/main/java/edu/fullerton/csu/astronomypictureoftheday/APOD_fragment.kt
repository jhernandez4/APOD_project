package edu.fullerton.csu.astronomypictureoftheday
import android.content.Context
import androidx.lifecycle.MutableLiveData
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import edu.fullerton.csu.astronomypictureoftheday.databinding.FragmentApodBinding
import java.util.Calendar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.net.Uri
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.util.GregorianCalendar
import java.util.Properties

private const val TAG = "APOD_fragment"
private var _binding: FragmentApodBinding? = null
private val binding get() = _binding!!

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

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                // Load the API key in Fragment
                val apiKey = loadApiKey(requireContext())
                dateViewModel.setApiKeyFromContext(requireContext())
                // Call the fetchPicture method with context
                observePicture()
                binding.btnNext.isEnabled = !dateViewModel.isCurrentDate()
                binding.btnPrev.isEnabled = !dateViewModel.isFirstDate()
            }
        }
        
        setFragmentResultListener(CalendarDatePicker.REQUEST_KEY_DATE){ _, bundle ->
            val newDate = bundle.getSerializable(CalendarDatePicker.BUNDLE_KEY_DATE) as Calendar
            Log.d(TAG, "Date picked is ${newDate.time}")
            dateViewModel.setDate(newDate.get(Calendar.YEAR), newDate.get(Calendar.MONTH), newDate.get(Calendar.DAY_OF_MONTH))
            binding.btnNext.isEnabled = !dateViewModel.isCurrentDate()
            binding.btnPrev.isEnabled = !dateViewModel.isFirstDate()
        }

        binding.apply {
            btnPrev.setOnClickListener {
                dateViewModel.decrementDate()
                btnNext.isEnabled = !dateViewModel.isCurrentDate()
                btnPrev.isEnabled = !dateViewModel.isFirstDate()
            }

            btnNext.setOnClickListener {
                dateViewModel.incrementDate()
                btnNext.isEnabled = !dateViewModel.isCurrentDate()
                btnPrev.isEnabled = !dateViewModel.isFirstDate()
                // update picture and description,author,etc from NASA
            }

            btnDatePicker.setOnClickListener {
                findNavController().navigate(APOD_fragmentDirections.selectDate(dateViewModel.currentDate))
//                val year = 1995
//                val month = 6
//                val day = 16
//
//                dateViewModel.setDate(year, month, day)
//                btnPrev.isEnabled = !dateViewModel.isFirstDate()
//                btnNext.isEnabled = !dateViewModel.isCurrentDate()
//                Log.d(TAG, "Date set by user: ${dateViewModel.currentDate.time}")
//                dateViewModel.fetchPicture()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun loadApiKey(context: Context): String {
        val properties = Properties()
        context.resources.openRawResource(R.raw.api_keys).use { inputStream ->
            properties.load(inputStream)
        }
        return properties.getProperty("nasa_api_key", "")
    }

    private fun observePicture() {
        dateViewModel.currentPicture.observe(viewLifecycleOwner) { astronomyPicture ->
            astronomyPicture?.let {
                Glide.with(this)
                    .load(it.url)
                    .placeholder(R.drawable.placeholder_background)
                    .error(R.drawable.error_image_background)
                    .into(binding.ivImage)

                binding.tvDesc.text = it.explanation
                binding.tvTitle.text = it.title
            }
        }
    }
}