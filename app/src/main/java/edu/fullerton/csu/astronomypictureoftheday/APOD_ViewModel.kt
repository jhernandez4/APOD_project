package edu.fullerton.csu.astronomypictureoftheday

import android.content.Context
import android.util.Log
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import java.util.Calendar
import java.util.Date
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

private const val TAG = "APOD_ViewModel"
const val CURRENT_INDEX_KEY = "CURRENT_INDEX_KEY"
const val BASE_URL = "https://api.nasa.gov/"
class APOD_ViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private var apiKey: String? = null

    init {
        viewModelScope.launch{
            fetchPicture()
        }
    }

    fun setApiKeyFromContext(context: Context) {
        apiKey = loadApiKey(context)
        if (apiKey.isNullOrEmpty()) {
            Log.e(TAG, "API Key could not be loaded or is empty")
        }
    }

    // commented out for JVM testing
//    init {
//        Log.d(TAG, "ViewModel instance created")
//    }
//
//    override fun onCleared() {
//        super.onCleared()
//        Log.d(TAG, "ViewModel instance about to be destroyed")
//    }
    val currentPicture: MutableLiveData<AstronomyPicture> = MutableLiveData()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val apiService: NasaApiService = retrofit.create(NasaApiService::class.java)



    private fun formatDate(): String {
        val year = currentDateCalendar.get(Calendar.YEAR)
        val month = currentDateCalendar.get(Calendar.MONTH) + 1  // Months are zero-indexed
        val day = currentDateCalendar.get(Calendar.DAY_OF_MONTH)
        return "$year-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
    }

    // fetchPicture method to use the stored API key
    fun fetchPicture() {
        apiKey?.let { key ->
            apiService.getAstronomyPictureOfTheDay(key, formatDate())
                .enqueue(object : Callback<AstronomyPicture> {
                    override fun onResponse(call: Call<AstronomyPicture>, response: Response<AstronomyPicture>) {
                        if (response.isSuccessful) {
                            currentPicture.postValue(response.body())
                        } else {
                            Log.e(TAG, "Error fetching picture: ${response.errorBody()?.string()}")
                            decrementDate()
                        }
                    }

                    override fun onFailure(call: Call<AstronomyPicture>, t: Throwable) {
                        Log.e(TAG, "Network error: ${t.message}")
                    }
                })
        } ?: Log.e(TAG, "API Key is not set")
    }


    private fun loadApiKey(context: Context): String {
        val properties = Properties()
        try {
            context.resources.openRawResource(R.raw.api_keys).use { inputStream ->
                properties.load(inputStream)
            }
            val apiKey = properties.getProperty("nasa_api_key", "")
            Log.d(TAG, "Loaded API Key: $apiKey")
            return apiKey
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
        currentDateCalendar.set(Calendar.MONTH, Month - 1)
        currentDateCalendar.set(Calendar.DAY_OF_MONTH, Day)
        fetchPicture()  // Fetch new picture data after the date is updated
    }
}