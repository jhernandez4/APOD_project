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
import android.text.method.ScrollingMovementMethod
import androidx.fragment.app.setFragmentResultListener
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.GregorianCalendar
import java.util.Properties

private const val TAG = "APOD_fragment"

class APOD_fragment : Fragment() {

    private val dateViewModel: APOD_ViewModel by viewModels()

    // create binding for xml file -> binding class made automatically by enabling ViewBinding
    private var _binding: FragmentApodBinding? = null
    private val binding
        get() = checkNotNull(_binding){
            "Cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ignore default boilerplate below
        // return super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentApodBinding.inflate(layoutInflater, container, false)
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
                updateUI()
                updateStar()
                binding.btnFavorite.setOnClickListener {
                    viewLifecycleOwner.lifecycleScope.launch {
                        if (dateViewModel.getFavoriteCount() == 1) {
                            dateViewModel.deleteFavorite(dateViewModel.getCurrentDateFormatted())
                        } else {
                            dateViewModel.addFavorite(binding.tvTitle.text.toString())
                        }
                        // Update star UI
                        updateStar()
                    }
                }
            }
        }
        
        setFragmentResultListener(CalendarDatePicker.REQUEST_KEY_DATE){ _, bundle ->
            val newDate = bundle.getSerializable(CalendarDatePicker.BUNDLE_KEY_DATE) as Calendar
            Log.d(TAG, "Date picked is ${newDate.time}")
            dateViewModel.setDate(newDate.get(Calendar.YEAR), newDate.get(Calendar.MONTH), newDate.get(Calendar.DAY_OF_MONTH))
            updateUI()
        }

        binding.apply {
            btnList.setOnClickListener{
                findNavController().navigate(R.id.show_favorites)
            }

            btnPrev.setOnClickListener {
                dateViewModel.decrementDate()
                updateUI()
            }

            btnNext.setOnClickListener {
                dateViewModel.incrementDate()
                updateUI()
                // update picture and description,author,etc from NASA
            }

            // This button requires safe call operator
            btnCurrent?.setOnClickListener{
                dateViewModel.setCurrentDate()
                updateUI()
            }

            btnDatePicker.setOnClickListener {
                findNavController().navigate(APOD_fragmentDirections.selectDate(dateViewModel.currentDate))
            }
            tvDesc.movementMethod = ScrollingMovementMethod()
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

    private fun updateUI(){
        binding.apply{
            btnNext.isEnabled = !dateViewModel.isCurrentDate()
            btnPrev.isEnabled = !dateViewModel.isFirstDate()
            btnDatePicker.apply {
                text = dateViewModel.getCurrentDateFormatted()
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            updateStar()
        }
    }

    private suspend fun updateStar(){
        withContext(Dispatchers.IO) {
            val favoriteCount = dateViewModel.getFavoriteCount()
            // Now update the UI on the main thread
            withContext(Dispatchers.Main) {
                binding.btnFavorite.setImageResource(
                    if (favoriteCount == 1) R.drawable.full_star else R.drawable.empty_star
                )
            }
        }
    }
}