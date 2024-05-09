package edu.fullerton.csu.astronomypictureoftheday

import android.content.Context
import androidx.room.Room
import edu.fullerton.csu.astronomypictureoftheday.database.FavoriteDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.lang.IllegalStateException

private const val DATABASE_NAME = "favorite-database"

class FavoriteRepository private constructor(context: Context){
    private val database: FavoriteDatabase = Room
        .databaseBuilder(
            context.applicationContext,
            FavoriteDatabase::class.java,
            DATABASE_NAME
        ).build()

    fun getFavorites(): Flow<List<Favorite>> = database.favoriteDao().getFavorites()

    suspend fun getFavoriteCount(date: String): Int = withContext(Dispatchers.IO) {
        database.favoriteDao().getFavoriteCount(date)
    }

    suspend fun deleteFavorite(date: String) = withContext(Dispatchers.IO) {
        database.favoriteDao().deleteFavorite(date)
    }

    suspend fun addFavorite(favorite: Favorite) = withContext(Dispatchers.IO) {
        database.favoriteDao().addFavorite(favorite)
    }

    companion object{
        private var INSTANCE: FavoriteRepository? = null

        fun initialize(context: Context) {
            if (INSTANCE == null) {
                INSTANCE = FavoriteRepository(context)
            }
        }

        fun get(): FavoriteRepository {
            return INSTANCE ?:
            throw IllegalStateException("CrimeRepository must be initialized")
        }
    }
}