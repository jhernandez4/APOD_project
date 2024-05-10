import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.core.Serializer
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.io.DataInputStream
import java.io.DataOutputStream
import java.io.InputStream
import java.io.OutputStream

data class DateDatastore(
    val month: Int,
    val day: Int,
    val year: Int
)

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
