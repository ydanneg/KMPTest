package com.ydanneg.kmp.di

import com.ydanneg.kmp.CurrencyDao
import com.ydanneg.kmp.DatabaseFactory
import com.ydanneg.kmp.QuoteDao
import com.ydanneg.kmp.createDataStore
import org.koin.dsl.module

actual val platformModule = module {
    single<CurrencyDao> { DatabaseFactory(get()).createDatabase().getDao() }
    single<QuoteDao> { DatabaseFactory(get()).createDatabase().getQuoteDao() }
    single {
        createDataStore {
            context.filesDir.resolve(dataStoreFileName).absolutePath
        }
    }
}
