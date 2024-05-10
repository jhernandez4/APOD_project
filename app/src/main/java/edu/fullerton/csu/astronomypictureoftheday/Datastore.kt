import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import androidx.datastore.dataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.InputStream
import java.io.OutputStream

data class DateDatastore(
    val month: Int,
    val day: Int,
    val year: Int
)

object DateDatastoreSerializer : Serializer<DateDatastore> {
    override val defaultValue: DateDatastore = DateDatastore(0, 0, 0)

    override suspend fun readFrom(input: InputStream): DateDatastore {
        val month = input.readInt()
        val day = input.readInt()
        val year = input.readInt()
        return DateDatastore(month, day, year)
    }

    override suspend fun writeTo(t: DateDatastore, output: OutputStream) {
        output.writeInt(t.month)
        output.writeInt(t.day)
        output.writeInt(t.year)
    }
}

val Context.dateDatastore: DataStore<DateDatastore> by dataStore(
    fileName = "date_datastore.pb",
    serializer = DateDatastoreSerializer
)

fun DataStore<DateDatastore>.getDate(): Flow<DateDatastore> {
    return this.data.map { it }
}

suspend fun DataStore<DateDatastore>.setDate(date: DateDatastore) {
    this.updateData { current ->
        current.copy(
            month = date.month,
            day = date.day,
            year = date.year
        )
    }
}
