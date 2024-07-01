import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class Ticker(
    val id: Int,
    val symbol: String,
    val price: String
)

data class UiState(
    val currencies: List<Ticker> = listOf(),
    val loading: Boolean = false
)

class AppViewModel : ViewModel() {

    private val api = CoinMarketCapApi()

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            setState { copy(loading = true) }
            runCatching {
                val currencies = api.getListings().data.map {
                    Ticker(
                        id = it.id,
                        symbol = it.symbol,
                        price = it.quote.findPrice("USD")

                    )
                }
                setState { copy(currencies = currencies) }

            }
            setState { copy(loading = false) }
        }
    }

    private fun Map<String, Quote>.findPrice(key: String = "USD"): String {
        return entries.find { entry -> entry.key == key }?.value?.price?.round() ?: "-"
    }

    private fun Double.round(): String = with(this.toString()) {
        "${substringBefore(".")}.${substringAfter(".").take(4)}"
    }

    private fun setState(reducer: UiState.() -> UiState) {
        _state.value = state.value.reducer()
    }
}