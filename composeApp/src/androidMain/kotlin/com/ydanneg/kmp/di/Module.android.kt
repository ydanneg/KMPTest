package com.ydanneg.kmp.di

import com.ydanneg.kmp.CurrencyDao
import com.ydanneg.kmp.DatabaseFactory
import com.ydanneg.kmp.QuoteDao
import com.ydanneg.kmp.createDataStore
import com.ydanneg.kmp.dataStoreFileName
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

actual val platformModule = module {
    single<CurrencyDao> { DatabaseFactory(get()).createDatabase().getDao() }
    single<QuoteDao> { DatabaseFactory(get()).createDatabase().getQuoteDao() }
    single {
        createDataStore {
            androidContext().filesDir.resolve(dataStoreFileName).absolutePath
        }
    }
}
