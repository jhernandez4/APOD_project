package edu.fullerton.csu.astronomypictureoftheday

data class AstronomyPicture(
    val date: String,
    val explanation: String,
    val url: String,
    val title: String,
    val media_type: String, // "image" or "video"
    val videoId: String? = null // Optional, use if videoId is separate
)