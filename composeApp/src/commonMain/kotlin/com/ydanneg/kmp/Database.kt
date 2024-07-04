@file:Suppress("ktlint:filename")

package com.ydanneg.kmp

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Database(
    entities = [
        CurrencyEntity::class,
        QuoteEntity::class
    ],
    version = 1
)
abstract class CurrencyDatabase : RoomDatabase() {
    abstract fun getDao(): CurrencyDao
    abstract fun getQuoteDao(): QuoteDao
}

@Dao
interface CurrencyDao {

    @Upsert
    suspend fun insert(items: List<CurrencyEntity>)

    @Query("SELECT * FROM currencies ORDER by rank ASC")
    fun getAll(): Flow<List<CurrencyEntity>>

    @Query("DELETE FROM currencies")
    suspend fun deleteAll()

    @Transaction
    @Query("SELECT * FROM currencies ORDER by rank ASC")
    fun getAllWithQuotes(): Flow<List<CurrencyWithQuotes>>
}

@Dao
interface QuoteDao {
    @Upsert
    suspend fun insert(items: List<QuoteEntity>)

    @Query("DELETE FROM quotes")
    suspend fun deleteAll()
}

expect class DatabaseFactory {
    fun createDatabase(): CurrencyDatabase
}

internal const val dbFileName = "currencies.db"
