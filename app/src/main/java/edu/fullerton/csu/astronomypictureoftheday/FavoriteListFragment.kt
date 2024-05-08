package edu.fullerton.csu.astronomypictureoftheday

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels

private const val TAG = "FavoriteListFragment"
class FavoriteListFragment : Fragment() {

    private val favoriteListViewModel: FavoriteListViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "Total crimes: ${favoriteListViewModel.favorites.size}")
    }
}