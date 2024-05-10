import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dateDataStore: DataStore<Preferences> by preferencesDataStore(name = "date_data_store")

class DateDataStore(context: Context) {
    data class DateData(val day: Int, val month: Int, val year: Int)

    private val dataStore = context.dateDataStore

    suspend fun saveDate(day: Int, month: Int, year: Int) {
        dataStore.edit { preferences ->
            preferences[KEY_DAY] = day
            preferences[KEY_MONTH] = month
            preferences[KEY_YEAR] = year
        }
    }

    val dateFlow: Flow<DateData> = dataStore.data
        .map { preferences ->
            val day = preferences[KEY_DAY] ?: DEFAULT_DAY
            val month = preferences[KEY_MONTH] ?: DEFAULT_MONTH
            val year = preferences[KEY_YEAR] ?: DEFAULT_YEAR
            DateData(day, month, year)
        }

    companion object {
        private val KEY_DAY = intPreferencesKey("day")
        private val KEY_MONTH = intPreferencesKey("month")
        private val KEY_YEAR = intPreferencesKey("year")

        private const val DEFAULT_DAY = 1
        private const val DEFAULT_MONTH = 1
        private const val DEFAULT_YEAR = 2024
    }
}
