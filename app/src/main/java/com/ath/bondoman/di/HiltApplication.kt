package com.ath.bondoman.di

import NetworkChangeReceiver
import android.app.Application
import android.content.IntentFilter
import android.net.ConnectivityManager
import dagger.hilt.android.HiltAndroidApp


@HiltAndroidApp
class HiltApplication: Application() {
    private var networkChangeReceiver: NetworkChangeReceiver? = null
    override fun onCreate() {
        super.onCreate()
    }
}