import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt

data class Ticker(
    val id: Int,
    val symbol: String,
    val name: String,
    val price: String,
    val btcPrice: String,
    val imageUrl: String,
    val rank: Int
)

data class UiState(
    val currencies: List<Ticker> = listOf(),
    val loading: Boolean = false,
    val error: String? = null
)

class AppViewModel(
    private val currencyDao: CurrencyDao
) : ViewModel() {

    private val api = CoinMarketCapApi()

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            fetchFromDatabase()
        }
//
//        viewModelScope.launch {
//            setState { copy(loading = true) }
//            runCatching {
//                fetchFromRemote()
//            }.onFailure {
//                println("error: ${it.message}")
//                setState { copy(error = "Error") }
//            }.onSuccess {
//                setState { copy(loading = false) }
//            }
//        }
    }

    private suspend fun fetchFromRemote() = withContext(Dispatchers.IO) {
        val entities = coroutineScope {
            val usd = async { api.getListings(priceCurrency = "USD") }
            val btc = async { api.getListings(priceCurrency = "BTC") }

            val byUsd = usd.await().data?.associateBy { it.symbol } ?: mapOf()
            val byBtc = btc.await().data?.associateBy { it.symbol } ?: mapOf()

            byUsd.entries.map {
                val currency = it.value
                Currency(
                    id = currency.id,
                    symbol = currency.symbol,
                    price = currency.quote.findPrice("USD"),
                    btcPrice = byBtc[currency.symbol]?.quote?.findPrice("BTC") ?: 0.0,
                    rank = currency.cmcRank,
                    name = currency.name
                )
            }
        }
        currencyDao.insert(entities)
    }

    private suspend fun fetchFromDatabase() = withContext(Dispatchers.IO) {
        currencyDao.getAll()
            .collect { items ->
                val tickers = items.map {
                    Ticker(
                        id = it.id,
                        symbol = it.symbol,
                        name = it.name,
                        price = "$${it.price.toPrecision(3)}",
                        btcPrice = "₿${it.btcPrice.toPrecision(8)}",
                        rank = it.rank,
                        imageUrl = "https://s2.coinmarketcap.com/static/img/coins/32x32/${it.id}.png"
                    )
                }
                setState {
                    copy(currencies = tickers)
                }
            }
    }

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

    private fun setState(reducer: UiState.() -> UiState) {
        _state.value = state.value.reducer()
    }
}