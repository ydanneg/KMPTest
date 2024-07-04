package com.ydanneg.kmp

import com.ydanneg.kmp.ui.QuoteCurrency
import kotlinx.coroutines.async
import kotlinx.coroutines.supervisorScope

class CurrencyRepository(
    private val api: CoinMarketCapApi,
    private val currencyDao: CurrencyDao,
    private val quoteDao: QuoteDao
) {

    val currencies = currencyDao.getAllWithQuotes()

    suspend fun updateFromRemote() {
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
