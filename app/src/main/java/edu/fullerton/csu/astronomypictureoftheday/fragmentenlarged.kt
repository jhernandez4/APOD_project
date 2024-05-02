import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

class fragmentenlarged: Fragment()
{

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)

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

    override fun onCreateView(
        inflater:LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_enlarge, container, false)
        //return super.onCreateView(inflater, container, savedInstanceState)
        return view
    }

    override fun onStart() {
        super.onStart()
    }
}