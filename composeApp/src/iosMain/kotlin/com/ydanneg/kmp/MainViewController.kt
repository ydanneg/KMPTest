package com.ydanneg.kmp

import androidx.compose.ui.window.ComposeUIViewController
import com.ydanneg.kmp.ui.App
import com.ydanneg.kmp.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    App()
}
