package com.ath.bondoman.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities


fun isNetworkAvailable(context: Context): Boolean {
    val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val networkInfo     = connectivityManager.activeNetwork ?: return false
    val activeNetworkInfo = connectivityManager.getNetworkCapabilities(networkInfo) ?: return false
    return(
        activeNetworkInfo.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
        activeNetworkInfo.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
        activeNetworkInfo.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
        activeNetworkInfo.hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)
    )
}
