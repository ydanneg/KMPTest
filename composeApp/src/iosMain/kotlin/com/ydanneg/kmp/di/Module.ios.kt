package com.ydanneg.kmp.di

import com.ydanneg.kmp.CurrencyDao
import com.ydanneg.kmp.DatabaseFactory
import com.ydanneg.kmp.QuoteDao
import com.ydanneg.kmp.createDataStore
import com.ydanneg.kmp.dataStoreFileName
import kotlinx.cinterop.ExperimentalForeignApi
import org.koin.dsl.module
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSURL
import platform.Foundation.NSUserDomainMask

@OptIn(ExperimentalForeignApi::class)
actual val platformModule = module {
    single<CurrencyDao> { DatabaseFactory().createDatabase().getDao() }
    single<QuoteDao> { DatabaseFactory().createDatabase().getQuoteDao() }
    single {
        createDataStore {
            val documentDirectory: NSURL? = NSFileManager.defaultManager.URLForDirectory(
                directory = NSDocumentDirectory,
                inDomain = NSUserDomainMask,
                appropriateForURL = null,
                create = false,
                error = null
            )
            requireNotNull(documentDirectory).path + "/$dataStoreFileName"
        }
    }
}
