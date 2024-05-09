package edu.fullerton.csu.astronomypictureoftheday

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.util.Date
import java.util.GregorianCalendar
import java.util.UUID

class FavoriteListViewModel : ViewModel() {
    val favorites = mutableListOf<Favorite>()

    init {
        viewModelScope.launch {
            favorites += loadFavorites()
        }
    }

    fun loadFavorites() : List<Favorite> {
        val result = mutableListOf<Favorite>()

        for (i in 1 until 31) {
            val favorite = Favorite(
                id = UUID.randomUUID(),
                title = "Favorite Picture #$i",
                date = GregorianCalendar(2024, 3, i)
            )

            result += favorite
        }

        return result
    }
}