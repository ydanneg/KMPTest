package com.ydanneg.kmp

import androidx.compose.ui.window.ComposeUIViewController
import com.ydanneg.kmp.di.initKoin
import com.ydanneg.kmp.ui.App

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    App()
}
