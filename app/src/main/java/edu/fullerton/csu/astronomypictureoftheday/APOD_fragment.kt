package edu.fullerton.csu.astronomypictureoftheday

import android.Manifest
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.app.AlertDialog
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import androidx.lifecycle.MutableLiveData
import android.os.Build
import android.os.Bundle
import android.os.Debug
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
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
import android.provider.MediaStore
import android.provider.Settings
import android.widget.ImageView
import android.text.method.ScrollingMovementMethod
import android.widget.Toast
import androidx.core.content.ContextCompat
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
import java.util.regex.Pattern

private const val TAG = "APOD_fragment"

class APOD_fragment : Fragment() {

    private val dateViewModel: APOD_ViewModel by viewModels()

    private var _binding: FragmentApodBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }

    // create binding for xml file -> binding class made automatically by enabling ViewBinding

    private var currentAnimator: Animator? = null
    private var shortAnimationDuration: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }

        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
    }

    override fun onCreateView(

        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentApodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupWebView()
        initObservers()
        setupUI()
        setFragmentResultListener(CalendarDatePicker.REQUEST_KEY_DATE) { _, bundle ->
            val newDate = bundle.getSerializable(CalendarDatePicker.BUNDLE_KEY_DATE) as Calendar
            Log.d(TAG, "Date picked is ${newDate.time}")
            dateViewModel.setDate(
                newDate.get(Calendar.YEAR),
                newDate.get(Calendar.MONTH),
                newDate.get(Calendar.DAY_OF_MONTH)
            )
            updateUI()
            dateViewModel.fetchPicture() // Call fetchPicture() when the date changes
        }

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
        // Set long press listener on the image to request storage permission
        binding.ivImage.setOnLongClickListener {
            requestStoragePermission()
            true // Indicate that the long press event is consumed.
        }
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
            ivImage.setOnClickListener {
                Log.d(TAG, "TEST")

                if (binding.expandedImage.visibility == View.VISIBLE) {
                    setDismissLargeImageAnimation(binding.ivImage, RectF(), 1f)
                } else {
                    zoomImageFromThumb(binding.ivImage)
                }
            }

            btnList.setOnClickListener {
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
            btnCurrent?.setOnClickListener {
                dateViewModel.setCurrentDate()
                updateUI()
            }

            btnDatePicker.setOnClickListener {
                findNavController().navigate(
                    APOD_fragmentDirections.selectDate(dateViewModel.currentDate)
                )
            }
            tvDesc.movementMethod = ScrollingMovementMethod()
        }
    }

    private fun zoomImageFromThumb(thumbView: ImageView) {
        // If there's an animation in progress, cancel it immediately and
        // proceed with this one.
        currentAnimator?.cancel()

        val drawable = thumbView.drawable
        if (drawable is BitmapDrawable) {
            val bitmap = drawable.bitmap
        }

        // Load the high-resolution "zoomed-in" image.
        //binding.expandedImage.setImageResource(imageResId)

        // Calculate the starting and ending bounds for the zoomed-in image.
        val startBoundsInt = Rect()
        val finalBoundsInt = Rect()
        val globalOffset = Point()

        // The start bounds are the global visible rectangle of the thumbnail,
        // and the final bounds are the global visible rectangle of the
        // container view. Set the container view's offset as the origin for the
        // bounds, since that's the origin for the positioning animation
        // properties (X, Y).
        thumbView.getGlobalVisibleRect(startBoundsInt)
        binding.main.getGlobalVisibleRect(finalBoundsInt, globalOffset)
        startBoundsInt.offset(-globalOffset.x, -globalOffset.y)
        finalBoundsInt.offset(-globalOffset.x, -globalOffset.y)

        val startBounds = RectF(startBoundsInt)
        val finalBounds = RectF(finalBoundsInt)

        // Using the "center crop" technique, adjust the start bounds to be the
        // same aspect ratio as the final bounds. This prevents unwanted
        // stretching during the animation. Calculate the start scaling factor.
        // The end scaling factor is always 1.0.
        val startScale: Float
        if ((finalBounds.width() / finalBounds.height() > startBounds.width() / startBounds.height())) {
            // Extend start bounds horizontally.
            startScale = startBounds.height() / finalBounds.height()
            val startWidth: Float = startScale * finalBounds.width()
            val deltaWidth: Float = (startWidth - startBounds.width()) / 2
            startBounds.left -= deltaWidth.toInt()
            startBounds.right += deltaWidth.toInt()
        } else {
            // Extend start bounds vertically.
            startScale = startBounds.width() / finalBounds.width()
            val startHeight: Float = startScale * finalBounds.height()
            val deltaHeight: Float = (startHeight - startBounds.height()) / 2f
            startBounds.top -= deltaHeight.toInt()
            startBounds.bottom += deltaHeight.toInt()
        }

        // Hide the thumbnail and show the zoomed-in view. When the animation
        // begins, it positions the zoomed-in view in the place of the
        // thumbnail.
        thumbView.alpha = 0f

        animateZoomToLargeImage(startBounds, finalBounds, startScale)

        setDismissLargeImageAnimation(thumbView, startBounds, startScale)
    }

    private fun animateZoomToLargeImage(startBounds: RectF, finalBounds: RectF, startScale: Float) {
        binding.expandedImage.visibility = View.VISIBLE
        binding.tvDesc.visibility = View.INVISIBLE

        // Set the pivot point for SCALE_X and SCALE_Y transformations to the
        // top-left corner of the zoomed-in view. The default is the center of
        // the view.
        binding.expandedImage.pivotX = 0f
        binding.expandedImage.pivotY = 0f

        // Construct and run the parallel animation of the four translation and
        // scale properties: X, Y, SCALE_X, and SCALE_Y.
        currentAnimator = AnimatorSet().apply {
            play(
                ObjectAnimator.ofFloat(
                    binding.expandedImage,
                    View.X,
                    startBounds.left,
                    finalBounds.left
                )
            ).apply {
                with(
                    ObjectAnimator.ofFloat(
                        binding.expandedImage,
                        View.Y,
                        startBounds.top,
                        finalBounds.top
                    )
                )
                with(ObjectAnimator.ofFloat(binding.expandedImage, View.SCALE_X, startScale, 1f))
                with(ObjectAnimator.ofFloat(binding.expandedImage, View.SCALE_Y, startScale, 1f))
            }
            duration = shortAnimationDuration.toLong()
            interpolator = DecelerateInterpolator()
            addListener(object : AnimatorListenerAdapter() {

                override fun onAnimationEnd(animation: Animator) {
                    currentAnimator = null
                }

                override fun onAnimationCancel(animation: Animator) {
                    currentAnimator = null
                }
            })
            start()
        }
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
                        binding.webView.visibility = View.VISIBLE
                    }
                } else {
                    Glide.with(this@APOD_fragment)
                        .load(astronomyPicture.url)
                        .placeholder(R.drawable.placeholder_background)
                        .error(R.drawable.error_image_background)
                        .into(binding.ivImage)
                    Glide.with(this@APOD_fragment)
                        .load(astronomyPicture.url)
                        .placeholder(R.drawable.placeholder_background)
                        .error(R.drawable.error_image_background)
                        .into(binding.expandedImage)
                    binding.tvDesc.text = astronomyPicture.explanation
                    binding.tvTitle.text = astronomyPicture.title

                    binding.ivImage.visibility = View.VISIBLE
                    binding.webView.visibility = View.GONE
                }
            }
        }
    }

    private fun updateUI() {
        binding.apply {
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

    private suspend fun updateStar() {
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

    private fun setDismissLargeImageAnimation(
        thumbView: View,
        startBounds: RectF,
        startScale: Float
    ) {
        // When the zoomed-in image is tapped, it zooms down to the original
        // bounds and shows the thumbnail instead of the expanded image.
        binding.expandedImage.setOnClickListener {
            currentAnimator?.cancel()

            // Animate the four positioning and sizing properties in parallel,
            // back to their original values.
            currentAnimator = AnimatorSet().apply {
                play(
                    ObjectAnimator.ofFloat(
                        binding.expandedImage,
                        View.X,
                        startBounds.left
                    )
                ).apply {
                    with(ObjectAnimator.ofFloat(binding.expandedImage, View.Y, startBounds.top))
                    with(ObjectAnimator.ofFloat(binding.expandedImage, View.SCALE_X, startScale))
                    with(ObjectAnimator.ofFloat(binding.expandedImage, View.SCALE_Y, startScale))
                }
                duration = shortAnimationDuration.toLong()
                interpolator = DecelerateInterpolator()
                addListener(object : AnimatorListenerAdapter() {

                    override fun onAnimationEnd(animation: Animator) {
                        thumbView.alpha = 1f
                        binding.expandedImage.visibility = View.GONE
                        binding.tvDesc.visibility = View.VISIBLE
                        currentAnimator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        thumbView.alpha = 1f
                        binding.expandedImage.visibility = View.GONE
                        binding.tvDesc.visibility = View.VISIBLE
                        currentAnimator = null
                    }
                })
                start()

            }
        }
    }

    private fun requestStoragePermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                showExplanationDialog()
            } else {
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_STORAGE_PERMISSION_CODE
                )
            }
        } else {
            saveImageToStorage()
        }
    }

    private fun showExplanationDialog() {
        AlertDialog.Builder(context)
            .setTitle("Permission Needed")
            .setMessage("This permission is needed to save images to your device.")
            .setPositiveButton("OK") { _, _ ->
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_STORAGE_PERMISSION_CODE
                )
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun showGoToSettingsDialog() {
        AlertDialog.Builder(context)
            .setTitle("Permission Denied")
            .setMessage("This app needs storage permission to save images. Please enable it in app settings.")
            .setPositiveButton("Settings") { dialog, which ->
                showAppSettings()
            }
            .setNegativeButton("Cancel", null)
            .create()
            .show()
    }

    private fun showAppSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", requireActivity().packageName, null)
        intent.data = uri
        startActivity(intent)
    }

    private fun saveImageToStorage() {
        val drawable = binding.ivImage.drawable as? BitmapDrawable
        val bitmap = drawable?.bitmap
        if (bitmap != null) {
            val savedImageURL = MediaStore.Images.Media.insertImage(
                requireActivity().contentResolver,
                bitmap,
                "APOD_${System.currentTimeMillis()}",
                "Image from Astronomy Picture of the Day"
            )
            if (savedImageURL != null) {
                Toast.makeText(context, "Image Saved Successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Failed to Save Image", Toast.LENGTH_SHORT).show()
            }
        }
    }

    companion object {
        private const val REQUEST_STORAGE_PERMISSION_CODE = 101
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_STORAGE_PERMISSION_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                saveImageToStorage()
            } else {
                if (!shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                    showGoToSettingsDialog()
                } else {
                    Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
        fun showAppSettings() {
            val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            val uri = Uri.fromParts("package", requireActivity().packageName, null)
            intent.data = uri
            startActivity(intent)
        }
    }
}
