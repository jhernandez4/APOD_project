package edu.fullerton.csu.astronomypictureoftheday

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebView
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
import java.util.Properties
import java.util.regex.Pattern

private const val TAG = "APOD_fragment"

class APOD_fragment : Fragment() {

    private val dateViewModel: APOD_ViewModel by viewModels()
    private var _binding: FragmentApodBinding? = null

    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebView()
        initObservers()
        setupUI()
    }

    private fun setupWebView() {
        binding.webView?.settings?.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            allowContentAccess = true
            allowFileAccess = true
            loadWithOverviewMode = true
            useWideViewPort = true
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            CookieManager.getInstance().setAcceptThirdPartyCookies(binding.webView, true)
        }

        binding.webView?.webChromeClient = object : WebChromeClient() {
            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
                Log.d(TAG, "WebView console message: ${consoleMessage?.message()}")
                return super.onConsoleMessage(consoleMessage)
            }
        }
    }

    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                val apiKey = loadApiKey(requireContext())
                dateViewModel.setApiKeyFromContext(requireContext())
                observePicture()
                updateUI()
            }
        }

        setFragmentResultListener(CalendarDatePicker.REQUEST_KEY_DATE){ _, bundle ->
            val newDate = bundle.getSerializable(CalendarDatePicker.BUNDLE_KEY_DATE) as Calendar
            Log.d(TAG, "Date picked is ${newDate.time}")
            dateViewModel.setDate(newDate.get(Calendar.YEAR), newDate.get(Calendar.MONTH), newDate.get(Calendar.DAY_OF_MONTH))
            updateUI()
        }

        dateViewModel.eventPlayVideo.observe(viewLifecycleOwner, { videoId ->
            Log.d(TAG, "Observing video playback event")
            if (videoId != null) {
                val cleanVideoId = extractVideoId(videoId)
                Log.d(TAG, "Video ID received: $cleanVideoId")
                loadVideoInWebView(cleanVideoId ?: videoId)
                binding.ivImage.visibility = View.GONE
                binding.webView?.visibility = View.VISIBLE
            } else {
                Log.d(TAG, "Received null Video ID, not displaying video")
                binding.webView?.visibility = View.GONE
            }
        })
    }

    private fun setupUI() {
        binding.apply {
            btnPrev.setOnClickListener {
                dateViewModel.decrementDate()
                updateButtonStates()
                updateUI()
            }

            btnNext.setOnClickListener {
                dateViewModel.incrementDate()
                updateButtonStates()
                updateUI()
                // update picture and description,author,etc from NASA
            }

            // This button requires safe call operator
            btnCurrent?.setOnClickListener{
                dateViewModel.setCurrentDate()
                updateUI()
            }

            btnDatePicker.setOnClickListener {
                findNavController().navigate(
                    APOD_fragmentDirections.selectDate(dateViewModel.currentDate)
                )
            }

            // Observe changes in the selected date
            setFragmentResultListener(CalendarDatePicker.REQUEST_KEY_DATE) { _, bundle ->
                val newDate = bundle.getSerializable(CalendarDatePicker.BUNDLE_KEY_DATE) as Calendar
                Log.d(TAG, "Date picked is ${newDate.time}")
                dateViewModel.setDate(newDate.get(Calendar.YEAR), newDate.get(Calendar.MONTH), newDate.get(Calendar.DAY_OF_MONTH))
                updateButtonStates()
                dateViewModel.fetchPicture() // Call fetchPicture() when the date changes
            }
        }
    }

    private fun updateButtonStates() {
        binding.btnNext.isEnabled = !dateViewModel.isCurrentDate()
        binding.btnPrev.isEnabled = !dateViewModel.isFirstDate()
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
            if (astronomyPicture != null) {
                if (astronomyPicture.media_type == "video") {
                    val videoId = extractVideoId(astronomyPicture.url)
                    videoId?.let {
                        loadVideoInWebView(getEmbedUrl(it))
                        binding.ivImage.visibility = View.GONE
                        binding.webView?.visibility = View.VISIBLE
                    }
                } else {
                    Glide.with(this@APOD_fragment)
                        .load(astronomyPicture.url)
                        .placeholder(R.drawable.placeholder_background)
                        .error(R.drawable.error_image_background)
                        .into(binding.ivImage)
                    binding.tvDesc.text = astronomyPicture.explanation
                    binding.tvTitle.text = astronomyPicture.title

                    binding.ivImage.visibility = View.VISIBLE
                    binding.webView?.visibility = View.GONE
                }
            }
        }
    }


    private fun extractVideoId(videoUrl: String): String? {
        val embedPattern = Pattern.compile("^https:\\/\\/www\\.youtube\\.com\\/embed\\/([^#&?]*).*")
        val regularPattern = Pattern.compile(
            "^https:\\/\\/(www\\.)?(youtube\\.com\\/watch\\?v=|youtu\\.be\\/|youtube\\.com\\/embed\\/)([^#&?]*).*",
            Pattern.CASE_INSENSITIVE
        )

        val embedMatcher = embedPattern.matcher(videoUrl)
        if (embedMatcher.matches()) {
            return embedMatcher.group(1)
        }

        val matcher = regularPattern.matcher(videoUrl)
        return if (matcher.matches()) {
            matcher.group(3)
        } else {
            null
        }
    }

    private fun loadVideoInWebView(videoId: String) {
        val embedUrl = "https://www.youtube.com/embed/$videoId"
        Log.d(TAG, "Embed URL: $embedUrl")

        val frameVideo = """
        <!DOCTYPE html>
        <html>
        <head>
        <style>
        body {
            margin: 0;
            padding: 0;
            overflow: hidden;
        }
        html, body, iframe {
            width: 100%;
            height: 100%;
        }
        </style>
        </head>
        <body>
        <iframe src="$embedUrl" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>
        </body>
        </html>
    """.trimIndent()

        binding.webView?.apply {
            loadDataWithBaseURL(null, frameVideo, "text/html", "utf-8", null)
            visibility = View.VISIBLE
        }
    }


    private fun getEmbedUrl(videoId: String): String {
        return "https://www.youtube.com/embed/$videoId"
    }

    override fun onDestroyView() {
        binding.webView?.destroy()
        super.onDestroyView()
        _binding = null
    }

    private fun updateUI() {
        binding.apply {
            btnNext.isEnabled = !dateViewModel.isCurrentDate()
            btnPrev.isEnabled = !dateViewModel.isFirstDate()
            btnDatePicker.apply {
                text = dateViewModel.getCurrentDateFormatted()
            }
        }
    }
}
