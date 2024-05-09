package edu.fullerton.csu.astronomypictureoftheday

import android.app.Application

class FavoriteIntentApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        FavoriteRepository.initialize(this)
    }
}