package edu.fullerton.csu.astronomypictureoftheday

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import edu.fullerton.csu.astronomypictureoftheday.databinding.FragmentFavoriteListBinding
import kotlinx.coroutines.launch

private const val TAG = "FavoriteListFragment"
class FavoriteListFragment : Fragment() {

    private var _binding: FragmentFavoriteListBinding? = null
    private val binding
        get() = checkNotNull(_binding) {
            "Cannot access binding because it is null. Is the view visible?"
        }
    private val favoriteListViewModel: FavoriteListViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoriteListBinding.inflate(inflater, container, false)

        binding.favoriteRecyclerView.layoutManager = LinearLayoutManager(context)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED){
                    favoriteListViewModel.favorites.collect{favorites ->
                        binding.favoriteRecyclerView.adapter =
                            FavoriteListAdapter(favorites) { favoriteDate ->
                            findNavController().navigate(FavoriteListFragmentDirections.showSingleFavorite(favoriteDate))
                        }
                    }

            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}