package com.ydanneg.kmp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.ydanneg.kmp.ui.App
import com.ydanneg.kmp.ui.AppContent
import com.ydanneg.kmp.ui.Change
import com.ydanneg.kmp.ui.QuoteCurrency
import com.ydanneg.kmp.ui.Ticker
import com.ydanneg.kmp.ui.TickerQuote
import com.ydanneg.kmp.ui.UiState

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            App()
        }
    }
}

@Composable
@Preview
private fun AppPreview() {
    MaterialTheme {
        Surface {
            AppContent(
                state = UiState(
                    currencies = listOf(
                        Ticker(
                            id = 1,
                            symbol = "BTC",
                            name = "Bitcoin",
                            imageUrl = "",
                            rank = 1,
                            quotes = mapOf(
                                QuoteCurrency.USD to TickerQuote(
                                    price = "100000",
                                    change1h = Change.Up("0.10"),
                                    change24h = Change.Down("0.02"),
                                    change7d = Change.Down("0.01")
                                ),
                                QuoteCurrency.BTC to TickerQuote(
                                    price = "1",
                                    change1h = Change.Up("0.10"),
                                    change24h = Change.Down("0.02"),
                                    change7d = Change.Down("0.01")
                                )
                            )
                        ),
                        Ticker(
                            id = 2,
                            symbol = "ETH",
                            name = "Ethereum",
                            imageUrl = "",
                            rank = 1,
                            quotes = mapOf(
                                QuoteCurrency.USD to TickerQuote(
                                    price = "100000",
                                    change1h = Change.Up("0.10"),
                                    change24h = Change.Down("0.02"),
                                    change7d = Change.Down("0.01")
                                ),
                                QuoteCurrency.BTC to TickerQuote(
                                    price = "1",
                                    change1h = Change.Up("0.10"),
                                    change24h = Change.Down("0.02"),
                                    change7d = Change.Down("0.01")
                                )
                            )
                        ),
                        Ticker(
                            id = 3,
                            symbol = "LTC",
                            name = "Litecoin",
                            imageUrl = "",
                            rank = 1,
                            quotes = mapOf(
                                QuoteCurrency.USD to TickerQuote(
                                    price = "100000",
                                    change1h = Change.Up("0.10"),
                                    change24h = Change.Down("0.02"),
                                    change7d = Change.Down("0.01")
                                ),
                                QuoteCurrency.BTC to TickerQuote(
                                    price = "1",
                                    change1h = Change.Up("0.10"),
                                    change24h = Change.Down("0.02"),
                                    change7d = Change.Down("0.01")
                                )
                            )
                        )
                    )
                )
            ) {
            }
        }
    }
}
