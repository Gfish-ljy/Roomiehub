package com.dorm.health.utils

import android.content.Context
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import androidx.core.content.ContextCompat
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

class NetworkMonitor(context: Context) {
    private val appContext = context.applicationContext
    private val connectivityManager =
        appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    val isOnline: Flow<Boolean> = callbackFlow {
        if (!hasNetworkStatePermission()) {
            trySend(true)
            awaitClose { }
            return@callbackFlow
        }

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                trySend(true)
            }

            override fun onLost(network: Network) {
                trySend(false)
            }
        }

        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        try {
            connectivityManager.registerNetworkCallback(request, callback)
            trySend(hasActiveNetwork())
            awaitClose { connectivityManager.unregisterNetworkCallback(callback) }
        } catch (_: SecurityException) {
            trySend(true)
            awaitClose { }
        }
    }.distinctUntilChanged()

    private fun hasNetworkStatePermission(): Boolean =
        ContextCompat.checkSelfPermission(
            appContext,
            android.Manifest.permission.ACCESS_NETWORK_STATE
        ) == PackageManager.PERMISSION_GRANTED

    fun hasActiveNetwork(): Boolean {
        val network = connectivityManager.activeNetwork ?: return false
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
}
