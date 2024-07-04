package com.ydanneg.kmp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ydanneg.kmp.CurrencyRepository
import com.ydanneg.kmp.CurrencyWithQuotes
import com.ydanneg.kmp.QuoteEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.pow
import kotlin.math.roundToInt

sealed class Change(open val percentage: String) {
    data class Up(override val percentage: String) : Change(percentage)
    data class Down(override val percentage: String) : Change(percentage)
}

enum class QuoteCurrency(val precision: Int, val symbol: Char) {
    USD(4, '$'),
    BTC(10, '₿')
}

data class Ticker(
    val id: Int,
    val symbol: String,
    val name: String,
    val imageUrl: String,
    val rank: Int,
    val quotes: Map<QuoteCurrency, TickerQuote>
) {
    val price
        get() = quotes[QuoteCurrency.USD]?.price ?: "-"
    val btcPrice
        get() = quotes[QuoteCurrency.BTC]?.price ?: "-"
}

data class TickerQuote(
    val price: String,
    val change1h: Change,
    val change24h: Change,
    val change7d: Change
)

data class UiState(
    val currencies: List<Ticker> = listOf(),
    val isLoading: Boolean = false,
    val error: String? = null
)

class AppViewModel(
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            currencyRepository.currencies.collect { currencies ->
                setState { copy(currencies = currencies.map { it.toUiModel() }) }
            }
        }
//        refresh()
    }

    fun refresh() = viewModelScope.launch {
        setState { copy(isLoading = true) }
        runCatching {
            currencyRepository.updateFromRemote()
        }.onFailure {
            println("error: ${it.message}")
            setState { copy(error = "Error") }
        }
        setState { copy(isLoading = false) }
    }

    private fun CurrencyWithQuotes.toUiModel() =
        Ticker(
            id = currency.id,
            symbol = currency.symbol,
            name = currency.name,
            rank = currency.rank,
            imageUrl = "https://s2.coinmarketcap.com/static/img/coins/32x32/${currency.id}.png",
            quotes = quotes.associate {
                it.currency to it.toUiModel()
            }
        )

    private fun QuoteEntity.toUiModel(): TickerQuote {
        return TickerQuote(
            price = "${currency.symbol}${price.toPrecision(currency.precision)}",
            change7d = change7d.asChange(),
            change1h = change1h.asChange(),
            change24h = change24h.asChange()
        )
    }

    private fun Double.asChange(): Change =
        if (this < 0) {
            Change.Down(toPrecision(2))
        } else
            Change.Up(toPrecision(2))

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
