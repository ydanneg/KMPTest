import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import platform.Foundation.NSHomeDirectory

actual class DatabaseFactory {
    actual fun createDatabase(): CurrencyDatabase {
        val dbFilePath = NSHomeDirectory() + dbFileName
        return Room.databaseBuilder<CurrencyDatabase>(
            name = dbFilePath,
            factory = { CurrencyDatabase::class.instantiateImpl() }
        ).apply {
            setDriver(BundledSQLiteDriver())
            setQueryCoroutineContext(Dispatchers.IO)
        }.build()
    }
}