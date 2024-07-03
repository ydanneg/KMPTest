import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt

class CurrencyRepository(
    private val api: CoinMarketCapApi,
    private val currencyDao: CurrencyDao
) {

    val currencies = currencyDao.getAll().map {
        it.map { it.toModel() }
    }

    private fun Currency.toModel() =
        Ticker(
            id = id,
            symbol = symbol,
            name = name,
            price = "$${price.toPrecision(3)}",
            btcPrice = "₿${btcPrice.toPrecision(8)}",
            rank = rank,
            imageUrl = "https://s2.coinmarketcap.com/static/img/coins/32x32/$id.png"
        )

    suspend fun updateFromRemote() = fetchFromRemote()

    private suspend fun fetchFromRemote() = withContext(Dispatchers.IO) {
        val entities = coroutineScope {
            val usd = async { api.getListings(priceCurrency = "USD") }
            val btc = async { api.getListings(priceCurrency = "BTC") }

            val byUsd = usd.await().data?.associateBy { it.symbol } ?: mapOf()
            val byBtc = btc.await().data?.associateBy { it.symbol } ?: mapOf()

            byUsd.entries.map {
                val currency = it.value
                val btcPrice = byBtc[currency.symbol]?.quote?.findPrice("BTC") ?: 0.0
                currency.toEntity(btcPrice)
            }
        }
        currencyDao.insert(entities)
    }

    private fun Cryptocurrency.toEntity(btcPrice: Double) =
        Currency(
            id = id,
            symbol = symbol,
            price = quote.findPrice("USD"),
            btcPrice = btcPrice,
            rank = cmcRank,
            name = name
        )

    private fun Map<String, Quote>.findPrice(key: String = "USD"): Double {
        return entries.find { entry -> entry.key == key }?.value?.price ?: 0.0
    }

    private fun Double.toPrecision(precision: Int) =
        if (precision < 1) {
            "${this.roundToInt()}"
        } else {
            val p = 10.0.pow(precision)
            val v = (abs(this) * p).roundToInt()
            val i = floor(v / p)
            var f = "${floor(v - (i * p)).toInt()}"
            while (f.length < precision) f = "0$f"
            val s = if (this < 0) "-" else ""
            "$s${i.toInt()}.$f"
        }
}
