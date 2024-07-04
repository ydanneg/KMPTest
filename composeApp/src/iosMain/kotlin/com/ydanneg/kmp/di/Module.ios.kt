package com.ydanneg.kmp.di

import com.ydanneg.kmp.CurrencyDao
import com.ydanneg.kmp.DatabaseFactory
import com.ydanneg.kmp.QuoteDao
import org.koin.dsl.module

actual val platformModule = module {
    single<CurrencyDao> { DatabaseFactory().createDatabase().getDao() }
    single<QuoteDao> { DatabaseFactory().createDatabase().getQuoteDao() }
}
