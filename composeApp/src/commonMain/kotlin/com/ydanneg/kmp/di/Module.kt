package com.ydanneg.kmp.di

import com.ydanneg.kmp.CoinMarketCapApi
import com.ydanneg.kmp.CurrencyRepository
import com.ydanneg.kmp.ui.AppViewModel
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val sharedModule = module {
    single<CoinMarketCapApi> {
        CoinMarketCapApi()
    }

    single<CurrencyRepository> {
        CurrencyRepository(get(), get(), get(), get())
    }

    viewModel { AppViewModel(get()) }
}

expect val platformModule: Module

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(sharedModule, platformModule)
    }
}
