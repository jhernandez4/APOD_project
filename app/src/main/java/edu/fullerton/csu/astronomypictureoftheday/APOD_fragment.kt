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
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.bumptech.glide.Glide
import kotlinx.coroutines.launch
import java.util.GregorianCalendar
import java.util.Properties
import android.content.Intent  // Required for Intent
import android.os.Build
import android.webkit.ConsoleMessage
import android.webkit.CookieManager
import android.webkit.WebChromeClient
import android.webkit.WebResourceError
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import java.util.regex.Pattern


private const val TAG = "APOD_fragment"
private var _binding: FragmentApodBinding? = null
private val binding get() = _binding!!

class APOD_fragment : Fragment() {

    private val dateViewModel: APOD_ViewModel by viewModels()

    // create binding for xml file -> binding class made automatically by enabling ViewBinding
    private lateinit var binding: FragmentApodBinding

//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//    }


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


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Enable WebView debugging on devices running Android KitKat and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
    }

//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
//                // Load the API key in Fragment
//                val apiKey = loadApiKey(requireContext())
//                dateViewModel.setApiKeyFromContext(requireContext())
//                // Call the fetchPicture method with context
//                observePicture()
//                binding.btnNext.isEnabled = !dateViewModel.isCurrentDate()
//                binding.btnPrev.isEnabled = !dateViewModel.isFirstDate()
//            }
//        }
//
//        binding.apply {
//            btnPrev.setOnClickListener {
//                dateViewModel.decrementDate()
//                btnNext.isEnabled = !dateViewModel.isCurrentDate()
//                btnPrev.isEnabled = !dateViewModel.isFirstDate()
//            }
//
//            btnNext.setOnClickListener {
//                dateViewModel.incrementDate()
//                btnNext.isEnabled = !dateViewModel.isCurrentDate()
//                btnPrev.isEnabled = !dateViewModel.isFirstDate()
//                // update picture and description,author,etc from NASA
//            }
//
//            btnDatePicker.setOnClickListener {
//                // get date from date selected by user
//                // set the date with -> dateViewModel.setDate(year, month, day)
//
//                // hard-coded values to test setDate functionality
//                // this is the first day an APOD was posted by NASA
//                // june 16, 1995
//                val year = 2024
//                val month = 5 // I think values are from 0 to 11. 5 is june
//                val day = 6
//
//                dateViewModel.setDate(year, month, day)
//                btnPrev.isEnabled = !dateViewModel.isFirstDate()
//                btnNext.isEnabled = !dateViewModel.isCurrentDate()
//                Log.d(TAG, "Date set by user: ${dateViewModel.currentDate.time}")
//                dateViewModel.fetchPicture()
//            }
//        }
//
//    }




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebView()  // Ensure WebView is configured on view creation

        initObservers()
        setupUI()
    }

//    private fun initObservers() {
//        viewLifecycleOwner.lifecycleScope.launch {
//            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
//                // Load the API key in Fragment
//                val apiKey = loadApiKey(requireContext())
//                dateViewModel.setApiKeyFromContext(requireContext())  // Pass the loaded API key to the ViewModel
//                observePicture()
//            }
//        }
//
//        // Move out of the coroutine block as LiveData observation is lifecycle-aware and doesn't require a coroutine scope
//        dateViewModel.eventPlayVideo.observe(viewLifecycleOwner, { videoId ->
//            Log.d(TAG, "Observing video playback event")
//            if (videoId != null) {
//                Log.d(TAG, "Video ID received: $videoId")
//                val intent = Intent(context, YouTubePlayerActivity::class.java).apply {
//                    putExtra("VIDEO_ID", videoId)
//                }
//                startActivity(intent)
//                Log.d(TAG, "YouTubePlayerActivity started")
//            } else {
//                Log.d(TAG, "Received null Video ID, not starting YouTubePlayerActivity")
//            }
//        })
//    }


    private fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                // Load the API key in Fragment
                val apiKey = loadApiKey(requireContext())
                dateViewModel.setApiKeyFromContext(requireContext())  // Pass the loaded API key to the ViewModel
                observePicture()
            }
        }

        // Observe the video ID from the ViewModel
        // Inside initObservers or observePicture where loadVideoInWebView is called
        dateViewModel.eventPlayVideo.observe(viewLifecycleOwner, { videoId ->
            Log.d(TAG, "Observing video playback event")
            if (videoId != null) {
                val cleanVideoId = extractVideoId(videoId)  // Clean videoId if necessary
                Log.d(TAG, "Video ID received: $cleanVideoId")
                loadVideoInWebView(cleanVideoId ?: videoId)
                binding.ivImage.visibility = View.GONE  // Hide the ImageView
                binding.webView?.visibility = View.VISIBLE  // Show the WebView
            } else {
                Log.d(TAG, "Received null Video ID, not displaying video")
                binding.webView?.visibility = View.GONE  // Ensure WebView is hidden if no video is to be displayed
            }
        })
    }







    private fun setupUI() {
        binding.apply {
            btnPrev.setOnClickListener {
                dateViewModel.decrementDate()
                updateButtonStates()
            }

            btnNext.setOnClickListener {
                dateViewModel.incrementDate()
                updateButtonStates()
            }

            btnDatePicker.setOnClickListener {
                // Ideally replace hardcoded values with a date picker dialog result
                val year = 2024
                val month = 5  // June (months are 0-indexed so 5 means June)
                val day = 6

                dateViewModel.setDate(year, month, day)
                updateButtonStates()
                Log.d(TAG, "Date set by user: ${dateViewModel.currentDate.time}")
                dateViewModel.fetchPicture()
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

//    private fun setupWebView() {
//        binding.webView?.settings?.apply {
//            javaScriptEnabled = true
//            domStorageEnabled = true  // Enable DOM storage API which is important for some scripts
//            allowContentAccess = true
//            allowFileAccess = true
//            loadWithOverviewMode = true
//            useWideViewPort = true
//        }
//
//        // Enable third-party cookies for WebView to handle external content like YouTube videos
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//            CookieManager.getInstance().setAcceptThirdPartyCookies(binding.webView, true)
//        }
//
//        // Set the WebChromeClient
//        binding.webView?.webChromeClient = object : WebChromeClient() {
//            override fun onConsoleMessage(consoleMessage: ConsoleMessage?): Boolean {
//                Log.d(TAG, "WebView console message: ${consoleMessage?.message()}")
//                return super.onConsoleMessage(consoleMessage)
//            }
//        }
//
//        // Set the WebViewClient
//        binding.webView?.webViewClient = object : WebViewClient() {
//            override fun onReceivedError(view: WebView?, request: WebResourceRequest?, error: WebResourceError?) {
//                super.onReceivedError(view, request, error)
//                Log.e(TAG, "WebView error: ${error?.description}")
//            }
//        }
//    }

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










//    private fun observePicture() {
//        dateViewModel.currentPicture.observe(viewLifecycleOwner) { astronomyPicture ->
//            astronomyPicture?.let {
//                Glide.with(this)
//                    .load(it.url)
//                    .placeholder(R.drawable.placeholder_background)
//                    .error(R.drawable.error_image_background)
//                    .into(binding.ivImage)
//
//                binding.tvDesc.text = it.explanation
//                binding.tvTitle.text = it.title
//            }
//        }
//    }


    private fun observePicture() {
        dateViewModel.currentPicture.observe(viewLifecycleOwner) { astronomyPicture ->
            if (astronomyPicture != null) {
                if (astronomyPicture.media_type == "video") {
                    val videoId = extractVideoId(astronomyPicture.url)
                    videoId?.let {
                        loadVideoInWebView(getEmbedUrl(it))
                        binding.ivImage.visibility = View.GONE  // Hide the ImageView
                        binding.webView?.visibility = View.VISIBLE  // Show the WebView
                    }
                } else {
                    Glide.with(this@APOD_fragment)
                        .load(astronomyPicture.url)
                        .placeholder(R.drawable.placeholder_background)
                        .error(R.drawable.error_image_background)
                        .into(binding.ivImage)
                    binding.tvDesc.text = astronomyPicture.explanation
                    binding.tvTitle.text = astronomyPicture.title

                    binding.ivImage.visibility = View.VISIBLE  // Show the ImageView
                    binding.webView?.visibility = View.GONE  // Hide the WebView
                }
            }
        }
    }






    fun extractVideoId(videoUrl: String): String? {
        // This regex will match both standard YouTube URLs and shortened URLs
        val embedPattern = Pattern.compile("^https:\\/\\/www\\.youtube\\.com\\/embed\\/([^#&?]*).*")
        val regularPattern = Pattern.compile(
            "^https:\\/\\/(www\\.)?(youtube\\.com\\/watch\\?v=|youtu\\.be\\/|youtube\\.com\\/embed\\/)([^#&?]*).*",
            Pattern.CASE_INSENSITIVE
        )

        // Check if it's already an embed URL
        val embedMatcher = embedPattern.matcher(videoUrl)
        if (embedMatcher.matches()) {
            return embedMatcher.group(1)  // Return the video ID directly
        }

        // Check for regular YouTube URLs
        val matcher = regularPattern.matcher(videoUrl)
        return if (matcher.matches()) {
            matcher.group(3)  // Return the video ID (group 3 from the regex)
        } else {
            null  // Return null if the URL is invalid
        }
    }





    private fun loadVideoInWebView(videoId: String) {
        val embedUrl = "https://www.youtube.com/embed/$videoId"  // Form the embed URL
        Log.d(TAG, "Embed URL: $embedUrl")

        val frameVideo = """
        <!DOCTYPE html>
        <html>
        <body>
        <!-- Embed YouTube video using the IFrame API -->
        <iframe width="100%" height="100%" src="$embedUrl" frameborder="0" allow="autoplay; encrypted-media" allowfullscreen></iframe>
        </body>
        </html>
    """.trimIndent()

        binding.webView?.apply {
            loadData(frameVideo, "text/html", "utf-8")
            visibility = View.VISIBLE  // Show the WebView
        }
    }





    private fun getEmbedUrl(videoId: String): String {
        // Assuming videoId is correctly extracted and does not need further processing
        return "https://www.youtube.com/embed/$videoId"
    }


//    private fun getEmbedUrl(videoUrl: String): String {
//        // Assuming videoUrl is a direct YouTube link or a video ID
//        return if (videoUrl.contains("youtube.com")) {
//            // Extract the video ID from the URL and return the embed URL
//            "https://www.youtube.com/embed/${videoUrl.substringAfter("v=")}"
//        } else {
//            "https://www.youtube.com/embed/$videoUrl"
//        }
//    }



    override fun onDestroyView() {
        binding.webView?.destroy()
        super.onDestroyView()
        _binding = null
    }





}