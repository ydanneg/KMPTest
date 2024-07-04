package com.ydanneg.kmp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.kamel.core.config.KamelConfig
import io.kamel.core.config.fileFetcher
import io.kamel.core.config.takeFrom
import io.kamel.image.KamelImage
import io.kamel.image.asyncPainterResource
import io.kamel.image.config.Default
import io.kamel.image.config.LocalKamelConfig
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(viewModel: AppViewModel = koinViewModel<AppViewModel>()) {
    MaterialTheme {
        val state = viewModel.state.collectAsState()
        AppContent(state.value) {
            viewModel.refresh()
        }
    }
}

@Composable
fun AppContent(state: UiState, onRefresh: () -> Unit = {}) {
    val pullRefreshState = rememberPullRefreshState(
        refreshing = state.isLoading,
        onRefresh = onRefresh
    )
    Box(
        modifier = Modifier.fillMaxSize().pullRefresh(pullRefreshState),
        contentAlignment = Alignment.Center
    ) {
        val lazyListState = rememberLazyListState()
        val kamelConfig = remember {
            KamelConfig {
                fileFetcher()
                imageBitmapCacheSize = 1000
                takeFrom(KamelConfig.Default)
            }
        }
        val currencies = state.currencies
        val rankWidth = measureTextWidth("100", MaterialTheme.typography.body2)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(16.dp),
            state = lazyListState
        ) {
            items(currencies, key = { it.id }) {
                Row(
                    modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        modifier = Modifier.width(rankWidth),
                        text = "${it.rank}",
                        style = MaterialTheme.typography.body2,
                        textAlign = TextAlign.Center
                    )
                    Spacer(Modifier.width(6.dp))
                    CompositionLocalProvider(LocalKamelConfig provides kamelConfig) {
                        KamelImage(
                            modifier = Modifier.size(24.dp),
                            resource = asyncPainterResource(it.imageUrl, key = it.id),
                            contentDescription = ""
                        )
                    }
                    Spacer(Modifier.width(8.dp))
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = it.symbol,
                            fontWeight = FontWeight.Bold,
                            style = MaterialTheme.typography.body2
                        )
                        Text(
                            text = it.name,
                            style = MaterialTheme.typography.caption
                        )
                    }
                    Column(Modifier.weight(1f)) {
                        Text(
                            text = it.price,
                            fontWeight = FontWeight.SemiBold,
                            style = MaterialTheme.typography.body2
                        )
                        Text(
                            text = it.btcPrice,
                            style = MaterialTheme.typography.caption
                        )
                    }
                    Column(Modifier.weight(1f), horizontalAlignment = Alignment.End) {
                        ChangeWidget(it.quotes[QuoteCurrency.USD]?.change24h ?: Change.Up("-"))
                        ChangeWidget(it.quotes[QuoteCurrency.BTC]?.change24h ?: Change.Up("-"))
                    }
                }
                Divider()
            }
        }
        PullRefreshIndicator(
            refreshing = state.isLoading,
            state = pullRefreshState,
            modifier = Modifier.align(Alignment.TopCenter)
        )
    }
}

@Composable
private fun measureTextWidth(text: String, style: TextStyle): Dp {
    val textMeasurer = rememberTextMeasurer()
    val widthInPixels = textMeasurer.measure(text, style).size.width
    return with(LocalDensity.current) { widthInPixels.toDp() }
}

@Composable
private fun ChangeWidget(change: Change) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = when (change) {
                is Change.Down -> Icons.Filled.ArrowDropDown
                is Change.Up -> Icons.Filled.ArrowDropUp
            },
            tint = when (change) {
                is Change.Down -> Color.Red
                is Change.Up -> Color.Green
            },
            contentDescription = ""
        )
        Text(
            text = "${change.percentage}%",
            style = MaterialTheme.typography.overline,
            color = when (change) {
                is Change.Down -> Color.Red
                is Change.Up -> Color.Green
            }
        )
    }
}
