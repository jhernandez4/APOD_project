package edu.fullerton.csu.astronomypictureoftheday

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import java.util.Calendar
import java.util.GregorianCalendar
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Properties
import java.util.UUID
import androidx.lifecycle.AndroidViewModel

private const val TAG = "APOD_ViewModel"
const val CURRENT_INDEX_KEY = "CURRENT_INDEX_KEY"
const val BASE_URL = "https://api.nasa.gov/"

class APOD_ViewModel(application: Application, private val savedStateHandle: SavedStateHandle) : AndroidViewModel(application) {
    private val favoriteRepository = FavoriteRepository.get()
    private var apiKey: String = ""
    val currentPicture: MutableLiveData<AstronomyPicture> = MutableLiveData()
    val eventPlayVideo = MutableLiveData<String>()
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: NasaApiService = retrofit.create(NasaApiService::class.java)

    init {
        apiKey = loadApiKey()
        if (apiKey.isEmpty()) {
            Log.e(TAG, "API Key is empty")
        }

        viewModelScope.launch{
            fetchPicture()
        }
    }

    private fun formatDate(): String {
        val year = currentDateCalendar.get(Calendar.YEAR)
        val month = currentDateCalendar.get(Calendar.MONTH) + 1  // Months are zero-indexed
        val day = currentDateCalendar.get(Calendar.DAY_OF_MONTH)
        return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }

    fun fetchPicture() {
        val date = formatDate()
        apiKey?.let { key ->
            apiService.getAstronomyPictureOfTheDay(key, date).enqueue(object : Callback<AstronomyPicture> {
                override fun onResponse(call: Call<AstronomyPicture>, response: Response<AstronomyPicture>) {
                    if (response.isSuccessful) {
                        response.body()?.let { apod ->
                            if (apod.media_type == "video") {
                                eventPlayVideo.postValue(apod.url) // Trigger event to play video
                            } else {
                                currentPicture.postValue(apod) // Update LiveData with the image data
                            }
                        }
                    } else {
                        Log.e(TAG, "Error fetching picture: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<AstronomyPicture>, t: Throwable) {
                    Log.e(TAG, "Network error: ${t.message}")
                }
            })
        } ?: Log.e(TAG, "API Key is not set")
    }

    fun setApiKeyFromContext(context: Context) {
        apiKey = loadApiKey()
        if (apiKey.isNullOrEmpty()) {
            Log.e(TAG, "API Key could not be loaded or is empty")
        }
    }

    private fun loadApiKey(): String {
        val properties = Properties()
        try {
            val context = getApplication<Application>().applicationContext
            context.resources.openRawResource(R.raw.api_keys).use { inputStream ->
                properties.load(inputStream)
            }
            return properties.getProperty("nasa_api_key", "")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load API key", e)
            return ""
        }
    }


    private var currentDateCalendar: Calendar
        // Retrieve the saved value or create a new Calendar instance if not available
        get() = savedStateHandle.get(CURRENT_INDEX_KEY) ?: GregorianCalendar().apply {
            savedStateHandle.set(CURRENT_INDEX_KEY, this)
        }
        // Update the saved value whenever currentDateCalendar is modified
        set(value) {
            savedStateHandle.set(CURRENT_INDEX_KEY, value)
        }

    val currentDate: Calendar
        get() = currentDateCalendar

    fun incrementDate() {
        currentDateCalendar.add(Calendar.DAY_OF_MONTH, 1)
        Log.d(TAG, "Incremented date by 1: ${currentDate.time}")
        fetchPicture()  // Fetch new picture whenever date is changed
    }

    fun decrementDate() {
        currentDateCalendar.add(Calendar.DAY_OF_MONTH, -1)
        Log.d(TAG, "Decremented date by 1: ${currentDate.time}")
        fetchPicture()  // Fetch new picture whenever date is changed
    }

    fun setDate(Year: Int, Month: Int, Day: Int){
        currentDateCalendar.set(Calendar.YEAR, Year)
        currentDateCalendar.set(Calendar.MONTH, Month)
        currentDateCalendar.set(Calendar.DAY_OF_MONTH, Day)
        Log.d(TAG, "Date set to: ${currentDate.time}")
        fetchPicture()  // Fetch new picture data after the date is updated
    }

    fun setCurrentDate(){
        val today = GregorianCalendar.getInstance()
        setDate(today.get(Calendar.YEAR), today.get(Calendar.MONTH), today.get(Calendar.DAY_OF_MONTH))
        Log.d(TAG, "Setting date to current date: ${today.time}")
        fetchPicture()
    }

    fun getCurrentDateFormatted(): String {
        val month = currentDate.get(Calendar.MONTH)
        val day = currentDate.get(Calendar.DAY_OF_MONTH)
        val year = currentDate.get(Calendar.YEAR)

        return "${month+1}/$day/$year"
    }

    fun isCurrentDate(): Boolean {
        val today = GregorianCalendar.getInstance()
        Log.d(TAG, "Today's date is: ${today.time}, but not curr variable")
        return currentDate.get(Calendar.YEAR) == today.get(Calendar.YEAR) &&
                currentDate.get(Calendar.MONTH) == today.get(Calendar.MONTH) &&
                currentDate.get(Calendar.DAY_OF_MONTH) == today.get(Calendar.DAY_OF_MONTH)
    }

    fun isFirstDate(): Boolean {
        Log.d(TAG, "Comparing first date with current date, curr is: ${currentDate.time}")
        return currentDate.get(Calendar.YEAR) == 1995 &&
                currentDate.get(Calendar.MONTH) == 5 &&
                currentDate.get(Calendar.DAY_OF_MONTH) == 16
    }

    suspend fun getFavoriteCount(): Int {
        return favoriteRepository.getFavoriteCount(getCurrentDateFormatted())
    }

    suspend fun addFavorite(title: String){
        val favorite = Favorite(UUID.randomUUID(), title, currentDate)
        favoriteRepository.addFavorite(favorite)
    }

    suspend fun deleteFavorite(date: String){
        favoriteRepository.deleteFavorite(getCurrentDateFormatted())
    }
}