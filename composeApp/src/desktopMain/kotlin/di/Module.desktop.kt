package di

import CurrencyDao
import DatabaseFactory
import org.koin.dsl.module

actual val platformModule = module {
    single<CurrencyDao> {
        DatabaseFactory().createDatabase().getDao()
    }
}
