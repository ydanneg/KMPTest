package com.ydanneg.kmp

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import com.ydanneg.kmp.ui.QuoteCurrency
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.supervisorScope
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

private val minimumSyncTimeoutDuration = 1.minutes

class CurrencyRepository(
    private val api: CoinMarketCapApi,
    private val currencyDao: CurrencyDao,
    private val quoteDao: QuoteDao,
    private val preferences: DataStore<Preferences>
) {

    val currencies = currencyDao.getAllWithQuotes()

    private val lastSyncPrefKey = longPreferencesKey("lastSync")

    private suspend fun isTimeToSync() =
        preferences.data.first()[lastSyncPrefKey]
            ?.let { Instant.fromEpochMilliseconds(it) }
            ?.let { it + minimumSyncTimeoutDuration < Clock.System.now() }
            ?: true

    suspend fun updateFromRemote() {
        if (!isTimeToSync()) {
            return
        }
        val entities: Map<CurrencyEntity, List<QuoteEntity>> = supervisorScope {
            val usd = async { api.getListings(priceCurrency = QuoteCurrency.USD.name) }
            val btc = async { api.getListings(priceCurrency = QuoteCurrency.BTC.name) }

            val byUsd = usd.await().data?.associateBy { it.symbol } ?: mapOf()
            val byBtc = btc.await().data?.associateBy { it.symbol } ?: mapOf()

            byUsd.entries.associate {
                val currency = it.value
                val entity = currency.toEntity()
                val quotes = listOfNotNull(
                    currency.quote.findQuote(QuoteCurrency.USD)?.toEntity(currency.id, QuoteCurrency.USD),
                    byBtc[currency.symbol]?.quote?.findQuote(QuoteCurrency.BTC)?.toEntity(currency.id, QuoteCurrency.BTC)
                )
                entity to quotes
            }
        }

        preferences.edit {
            it[lastSyncPrefKey] = Clock.System.now().toEpochMilliseconds()
        }

        quoteDao.deleteAll()
        quoteDao.insert(entities.values.flatten())
        currencyDao.insert(entities.keys.toList())
    }

    private fun Cryptocurrency.toEntity() =
        CurrencyEntity(
            id = id,
            symbol = symbol,
            rank = cmcRank,
            name = name
        )

    private fun Quote.toEntity(currencyId: Int, currency: QuoteCurrency): QuoteEntity {
        return QuoteEntity(
            currencyId = currencyId,
            currency = currency,
            price = price,
            change7d = percentChange7d,
            change24h = percentChange24h,
            change1h = percentChange1h
        )
    }

    private fun Map<String, Quote>.findQuote(key: QuoteCurrency): Quote? =
        entries.find { entry -> entry.key == key.name }?.value
}
