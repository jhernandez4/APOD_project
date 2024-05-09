package edu.fullerton.csu.astronomypictureoftheday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Date
import java.util.GregorianCalendar
import java.util.UUID

class FavoriteListViewModel : ViewModel() {
    private val favoriteRepository = FavoriteRepository.get()
    val favorites = mutableListOf<Favorite>()

    init {
        viewModelScope.launch {
            favorites += loadFavorites()
        }
    }

    suspend fun loadFavorites() : List<Favorite> {
        return favoriteRepository.getFavorites()
    }
}