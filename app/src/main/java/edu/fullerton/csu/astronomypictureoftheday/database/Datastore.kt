import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.createDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.firstOrNull
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

// Define your data class
data class DateDatastore(
    val month: Int,
    val day: Int,
    val year: Int
)

// Serializer for your data class
object DateDatastoreSerializer : Serializer<DateDatastore> {
    override val defaultValue: DateDatastore = DateDatastore(0, 0, 0)

    override fun readFrom(input: InputStream): DateDatastore {
        val dataInput = DataInputStream(input)
        val month = dataInput.readInt()
        val day = dataInput.readInt()
        val year = dataInput.readInt()
        return DateDatastore(month, day, year)
    }

    override fun writeTo(t: DateDatastore, output: OutputStream) {
        val dataOutput = DataOutputStream(output)
        dataOutput.writeInt(t.month)
        dataOutput.writeInt(t.day)
        dataOutput.writeInt(t.year)
    }
}

// Extension function to get the data from DataStore
fun DataStore<Preferences>.getDate(): Flow<DateDatastore?> {
    return this.data.map { preferences ->
        preferences[PreferencesKey] ?: DateDatastoreSerializer.defaultValue
    }
}

// Extension function to set the data in DataStore
suspend fun DataStore<Preferences>.setDate(date: DateDatastore) {
    this.edit { preferences ->
        preferences[PreferencesKey] = date
    }
}

// Preferences key for DataStore
private object PreferencesKey : Preferences.Key<DateDatastore>("date_datastore")

// Context extension property to get DataStore instance
val Context.dateDatastore: DataStore<Preferences> by lazy {
    createDataStore(
        name = "date_datastore",
        serializer = DateDatastoreSerializer
    )
}
