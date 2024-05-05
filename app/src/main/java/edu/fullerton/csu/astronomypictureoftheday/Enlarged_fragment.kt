import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import edu.fullerton.csu.astronomypictureoftheday.APOD_fragment
import edu.fullerton.csu.astronomypictureoftheday.databinding.FragmentEnlargeBinding

class Enlarged_fragment: Fragment()
{
    private var _binding: FragmentEnlargeBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "cannot access binding because it is null. Is the view visible?"
        }

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater:LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEnlargeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            linearLayout.setOnClickListener { view ->

                val clickX = view.x + view.left
                val clickY = view.y + view.top

                val imageViewX = imageView.x + imageView.left
                val imageViewY = imageView.y + imageView.top

                val imageViewWidth = imageView.width
                val imageViewHeight = imageView.height

                if (clickX < imageViewX || clickX > imageViewX + imageViewWidth || clickY < imageViewY || clickY > imageViewY + imageViewHeight)
                {
                    // switching between fragments should be done using navigation graph
                    // wait on implementing this fully
//                    val intent = Intent(this, APOD_fragment::class.java)
//                    startActivity(intent)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}