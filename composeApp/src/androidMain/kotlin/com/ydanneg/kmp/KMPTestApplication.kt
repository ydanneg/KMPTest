package com.ydanneg.kmp

import android.app.Application
import di.initKoin
import org.koin.android.ext.koin.androidContext

class KMPTestApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@KMPTestApplication)
        }
    }
}
