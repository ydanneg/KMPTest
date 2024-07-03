package com.ydanneg.kmp.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
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
        val pullRefreshState = rememberPullRefreshState(
            refreshing = state.value.loading,
            onRefresh = viewModel::refresh
        )
        Box(
            modifier = Modifier.fillMaxSize().padding(16.dp).pullRefresh(pullRefreshState),
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
            val currencies = state.value.currencies
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                state = lazyListState
            ) {
                items(currencies, key = { it.id }) {
                    Row(
                        modifier = Modifier.fillMaxWidth().defaultMinSize(minHeight = 48.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "${it.rank}")
                        Spacer(Modifier.width(8.dp))
                        CompositionLocalProvider(LocalKamelConfig provides kamelConfig) {
                            KamelImage(
                                modifier = Modifier.size(24.dp),
                                resource = asyncPainterResource(it.imageUrl, key = it.id),
                                contentDescription = ""
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = it.symbol,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.body1
                            )
                            Text(
                                text = it.name,
                                style = MaterialTheme.typography.subtitle1
                            )
                        }
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = it.price,
                                fontWeight = FontWeight.SemiBold,
                                style = MaterialTheme.typography.body1
                            )
                            Text(
                                text = it.btcPrice,
                                style = MaterialTheme.typography.body2
                            )
                        }
                    }
                    Divider()
                }
            }
            PullRefreshIndicator(
                refreshing = state.value.loading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter)
            )
        }
    }
}
