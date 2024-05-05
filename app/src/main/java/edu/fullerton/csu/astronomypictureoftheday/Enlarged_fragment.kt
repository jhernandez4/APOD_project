import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import edu.fullerton.csu.astronomypictureoftheday.APOD_fragment
import edu.fullerton.csu.astronomypictureoftheday.databinding.FragmentEnlargeBinding

class FragmentEnlarged: Fragment()
{
    private lateinit var binding: FragmentEnlargeBinding

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater:LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentEnlargeBinding.inflate(layoutInflater, container, false)
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

                if(clickX < imageViewX || clickX > imageViewX + imageViewWidth || clickY < imageViewY || clickY > imageViewY + imageViewHeight)
                {
                    val intent = Intent(this, APOD_fragment::class.java)
                    startActivity(intent)
                }
            }
        }
    }
    override fun onStart() {
        super.onStart()
    }
}