package edu.fullerton.csu.astronomypictureoftheday.database

import androidx.room.TypeConverter
import java.util.Calendar
import java.util.GregorianCalendar

class FavoriteTypeConverters {
    @TypeConverter
    fun fromCalendar(calendar: Calendar): String {
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val year = calendar.get(Calendar.YEAR)

        return "$month/$day/$year"
    }

    @TypeConverter
    fun toCalendar(calendarString: String): Calendar {
        val parts = calendarString.split("/")
        val month = parts[0].toInt() - 1
        val day = parts[1].toInt()
        val year = parts[2].toInt()

        val calendar = GregorianCalendar(year, month, day)

        return calendar
    }
}