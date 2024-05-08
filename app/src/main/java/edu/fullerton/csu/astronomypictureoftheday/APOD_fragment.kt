package edu.fullerton.csu.astronomypictureoftheday

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.graphics.Point
import android.graphics.Rect
import android.graphics.RectF
import android.os.Bundle
import android.os.Debug
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator
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

    private var currentAnimator: Animator? = null
    private var shortAnimationDuration: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        shortAnimationDuration = resources.getInteger(android.R.integer.config_shortAnimTime)
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

            ivImage.setOnClickListener {
                Log.d(TAG, "TEST")
                //zoomImageFromThumb(expandedImage, R.drawable.image1)
            }

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

    private fun zoomImageFromThumb(thumbView: View, imageResId: Int) {
        // If there's an animation in progress, cancel it immediately and
        // proceed with this one.
        currentAnimator?.cancel()

        // Load the high-resolution "zoomed-in" image.
        binding.expandedImage.setImageResource(imageResId)

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
                    finalBounds.left)
            ).apply {
                with(ObjectAnimator.ofFloat(binding.expandedImage, View.Y, startBounds.top, finalBounds.top))
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
    private fun setDismissLargeImageAnimation(thumbView: View, startBounds: RectF, startScale: Float) {
        // When the zoomed-in image is tapped, it zooms down to the original
        // bounds and shows the thumbnail instead of the expanded image.
        binding.expandedImage.setOnClickListener {
            currentAnimator?.cancel()

            // Animate the four positioning and sizing properties in parallel,
            // back to their original values.
            currentAnimator = AnimatorSet().apply {
                play(ObjectAnimator.ofFloat(binding.expandedImage, View.X, startBounds.left)).apply {
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
                        currentAnimator = null
                    }

                    override fun onAnimationCancel(animation: Animator) {
                        thumbView.alpha = 1f
                        binding.expandedImage.visibility = View.GONE
                        currentAnimator = null
                    }
                })
                start()
            }
        }
    }


}