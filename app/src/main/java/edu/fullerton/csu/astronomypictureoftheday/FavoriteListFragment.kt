package edu.fullerton.csu.astronomypictureoftheday

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import edu.fullerton.csu.astronomypictureoftheday.databinding.FragmentFavoriteListBinding

private const val TAG = "FavoriteListFragment"
class FavoriteListFragment : Fragment() {

    private var _binding: FragmentFavoriteListBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    private val favoriteListViewModel: FavoriteListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Total crimes: ${favoriteListViewModel.favorites.size}")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoriteListBinding.inflate(inflater, container, false)

        binding.favoriteRecyclerView.layoutManager = LinearLayoutManager(context)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}