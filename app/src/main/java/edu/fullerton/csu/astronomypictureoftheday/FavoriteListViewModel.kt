package edu.fullerton.csu.astronomypictureoftheday

import androidx.lifecycle.ViewModel
import java.util.Date
import java.util.GregorianCalendar
import java.util.UUID

class FavoriteListViewModel : ViewModel() {
    val favorites = mutableListOf<Favorite>()

    init {
        for (i in 1 until 31) {
            val favorite = Favorite(
                id = UUID.randomUUID(),
                title = "Favorite Picture #$i",
                date = GregorianCalendar(2024, 3, i)
            )

            favorites += favorite
        }
    }
}