package edu.fullerton.csu.astronomypictureoftheday

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class Datastore private constructor(context: Context) {

    private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "ui_mode_preference")

    private val dataStore = context.dataStore

    suspend fun saveToDataStore(isNightMode: Boolean) {
        dataStore.edit { preferences ->
            preferences[UI_MODE_KEY] = isNightMode
        }
    }

    val uiMode: Flow<Boolean> = dataStore.data
        .map { preferences ->
            preferences[UI_MODE_KEY] ?: false
        }

    companion object {
        private val UI_MODE_KEY = booleanPreferencesKey("ui_mode")

        @Volatile
        private var INSTANCE: Datastore? = null

        fun getInstance(context: Context): Datastore {
            return INSTANCE ?: synchronized(this) {
                val instance = Datastore(context.applicationContext)
                INSTANCE = instance
                instance
            }
        }
    }
}
