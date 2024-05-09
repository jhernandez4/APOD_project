package edu.fullerton.csu.astronomypictureoftheday.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import edu.fullerton.csu.astronomypictureoftheday.Favorite
import java.util.Calendar

@Dao
interface FavoriteDao {
    @Query("SELECT * FROM favorite")
    suspend fun getFavorites(): List<Favorite>

    // date is  in MM/DD/YYYY -> month range is from 1-12
    // function returns 1 if exists, 0 if otherwise
    @Query("SELECT COUNT(*) FROM Favorite WHERE date=(:date)")
    fun getFavoriteCount(date: String): Int

    @Query("DELETE FROM favorite WHERE date=(:date)")
    suspend fun deleteFavorite(date: String)

    @Insert
    suspend fun addFavorite(favorite: Favorite)
}