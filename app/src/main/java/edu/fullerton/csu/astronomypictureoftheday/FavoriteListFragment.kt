package edu.fullerton.csu.astronomypictureoftheday

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.Switch
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
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

    private lateinit var datastore: Datastore


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentFavoriteListBinding.inflate(inflater, container, false)

        binding.favoriteRecyclerView.layoutManager = LinearLayoutManager(context)
        datastore = Datastore.getInstance(requireContext())

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                favoriteListViewModel.favorites.collect { favorites ->
                    binding.favoriteRecyclerView.adapter =
                        FavoriteListAdapter(
                            favorites,
                            onCrimeClicked = { favoriteDate ->
                                findNavController().navigate(
                                    FavoriteListFragmentDirections.showSingleFavorite(
                                        favoriteDate
                                    )
                                )
                            },

                            onCrimeLongClicked = { favoriteDate ->
                                viewLifecycleOwner.lifecycleScope.launch {
                                    favoriteListViewModel.deleteFavorite(favoriteDate)
                                }

                            })

                }

            }
        }
        val lightSwitch = binding.root.findViewById<Switch>(R.id.lightSwitch)

        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                datastore.uiMode.collect { isNightMode ->
                    setUIMode(isNightMode)
                    lightSwitch.isChecked = isNightMode
                }
            }

            lightSwitch.setOnCheckedChangeListener { _, isChecked ->
                setUIMode(isChecked)
                viewLifecycleOwner.lifecycleScope.launch {
                    datastore.saveToDataStore(isChecked)
                }
            }
        }
    }
    private fun setUIMode(isNightMode: Boolean) {
        if (isNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}