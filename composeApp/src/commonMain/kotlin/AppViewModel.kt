import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Ticker(
    val id: Int,
    val symbol: String,
    val name: String,
    val price: String,
    val btcPrice: String,
    val imageUrl: String
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
            currencyDao.getAll()
                .collect { items ->
                    println("items: #${items.size}")
                    setState {
                        copy(currencies = items.map {
                            Ticker(
                                id = it.id,
                                symbol = it.symbol,
                                name = it.name,
                                price = it.price,
                                btcPrice = it.btcPrice,
                                imageUrl = "https://s2.coinmarketcap.com/static/img/coins/32x32/${it.id}.png"
                            )
                        })
                    }
                }
        }
        viewModelScope.launch {
            setState { copy(loading = true) }
            coroutineScope {
                runCatching {
                    val usd = async {
                        api.getListings(priceCurrency = "USD")
                    }
                    val btc = async {
                        api.getListings(priceCurrency = "BTC")
                    }

                    val byUsd = usd.await().data?.associateBy { it.symbol } ?: mapOf()
                    val byBtc = btc.await().data?.associateBy { it.symbol } ?: mapOf()

                    val entities = byUsd.entries.map {
                        val currency = it.value
                        Currency(
                            id = currency.id,
                            symbol = currency.symbol,
                            price = "$${currency.quote.findPrice("USD").round(4)}",
                            btcPrice = "₿${byBtc[currency.symbol]?.quote?.findPrice("BTC")?.round(8)}",
                            name = currency.name
                        )
                    }
                    currencyDao.insert(entities)
                }.onFailure {
                    println("error: ${it.message}")
                    setState { copy(error = "Error") }
                }.onSuccess {
                    setState { copy(loading = false) }
                }
            }
        }
    }

    private fun Map<String, Quote>.findPrice(key: String = "USD"): String {
        return entries.find { entry -> entry.key == key }?.value?.price ?: "0.0"
    }

    private fun String.round(decimals: Int = 4): String {
        if (contains(".")) {
            return "${substringBefore(".")}.${substringAfter(".").take(decimals)}"
        }
        return this
    }

    private fun setState(reducer: UiState.() -> UiState) {
        _state.value = state.value.reducer()
    }
}