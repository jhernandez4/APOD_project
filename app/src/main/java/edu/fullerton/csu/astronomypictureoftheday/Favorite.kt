package edu.fullerton.csu.astronomypictureoftheday

import java.util.Date
import java.util.UUID

data class Favorite (val id: UUID,
                     val title: String,
                     val date: Date){
}