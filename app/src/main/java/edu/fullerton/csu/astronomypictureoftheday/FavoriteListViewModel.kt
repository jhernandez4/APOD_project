package edu.fullerton.csu.astronomypictureoftheday

import androidx.lifecycle.ViewModel
import java.util.Date
import java.util.UUID

class FavoriteListViewModel : ViewModel() {
    val favorites = mutableListOf<Favorite>()

    init {
        for (i in 0 until 100) {
            val favorite = Favorite(
                id = UUID.randomUUID(),
                title = "Favorite Picture #$i",
                date = Date()
            )

            favorites += favorite
        }
    }
}