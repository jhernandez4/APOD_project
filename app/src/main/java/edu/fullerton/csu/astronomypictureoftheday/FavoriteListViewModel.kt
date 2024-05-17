package edu.fullerton.csu.astronomypictureoftheday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.Date
import java.util.GregorianCalendar
import java.util.UUID

class FavoriteListViewModel : ViewModel() {


    private val favoriteRepository = FavoriteRepository.get()

     suspend fun deleteFavorite(favoriteDate: String) {
         viewModelScope.launch {

             favoriteRepository.deleteFavorite(favoriteDate)
         }

    }

    private val _favorites: MutableStateFlow<List<Favorite>> = MutableStateFlow(emptyList())
    val favorites: StateFlow<List<Favorite>>
        get() = _favorites.asStateFlow()

    init {
        viewModelScope.launch {
            favoriteRepository.getFavorites().collect {
                _favorites.value = it
            }
        }
    }
}