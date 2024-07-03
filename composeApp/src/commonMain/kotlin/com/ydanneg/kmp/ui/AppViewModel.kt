package com.ydanneg.kmp.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ydanneg.kmp.CurrencyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

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
    private val currencyRepository: CurrencyRepository
) : ViewModel() {

    private val _state = MutableStateFlow(UiState())
    val state = _state.asStateFlow()

    init {
        viewModelScope.launch {
            currencyRepository.currencies.collect {
                setState { copy(currencies = it) }
            }
        }

        refresh()
    }

    fun refresh() = viewModelScope.launch {
        setState { copy(loading = true) }
        runCatching {
            currencyRepository.updateFromRemote()
        }.onFailure {
            println("error: ${it.message}")
            setState { copy(error = "Error", loading = false) }
        }.onSuccess {
            setState { copy(loading = false) }
        }
    }

    private fun setState(reducer: UiState.() -> UiState) {
        _state.value = state.value.reducer()
    }
}
