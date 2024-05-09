package edu.fullerton.csu.astronomypictureoftheday.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import edu.fullerton.csu.astronomypictureoftheday.Favorite

@Database(entities = [Favorite::class], version=1, exportSchema = false)
@TypeConverters(FavoriteTypeConverters::class)
abstract class FavoriteDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
}