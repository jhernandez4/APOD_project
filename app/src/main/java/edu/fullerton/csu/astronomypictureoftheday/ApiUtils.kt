package edu.fullerton.csu.astronomypictureoftheday

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiUtils {
    private const val BASE_URL = "https://api.nasa.gov/"

    val nasaApiService: NasaApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NasaApiService::class.java)
    }
}