import android.content.Context
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers

actual class DatabaseFactory(private val context: Context) {
    actual fun createDatabase(): CurrencyDatabase {
        val appContext = context.applicationContext
        val dbFile = appContext.getDatabasePath(dbFileName)
        return Room.databaseBuilder<CurrencyDatabase>(
            context = appContext,
            name = dbFile.absolutePath
        ).apply {
            setQueryCoroutineContext(Dispatchers.IO)
            setDriver(BundledSQLiteDriver())
        }.build()
    }
}