@file:Suppress("ktlint:filename")

package com.ydanneg.kmp

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Database(entities = [Currency::class], version = 1)
abstract class CurrencyDatabase : RoomDatabase() {
    abstract fun getDao(): CurrencyDao
}

@Dao
interface CurrencyDao {

    @Upsert
    suspend fun insert(item: List<Currency>)

    @Query("SELECT * FROM Currency ORDER by rank ASC")
    fun getAll(): Flow<List<Currency>>

    @Query("DELETE FROM Currency")
    suspend fun deleteAll()
}

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class DatabaseFactory {
    fun createDatabase(): CurrencyDatabase
}

internal const val dbFileName = "currencies.db"
