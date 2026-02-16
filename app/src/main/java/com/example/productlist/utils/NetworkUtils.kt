package com.example.productlist.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext

sealed class ConnectionState {
    data object Available : ConnectionState()
    data object Unavailable : ConnectionState()
}

/**
 * Composable that returns the current network connection state, and recomposes when it changes.
 */
@Composable
fun currentConnectionState(): ConnectionState {
    val context = LocalContext.current
    var connectionState by remember { mutableStateOf<ConnectionState>(ConnectionState.Unavailable) }

    DisposableEffect(Unit) {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val callback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                connectionState = ConnectionState.Available
            }

            override fun onLost(network: Network) {
                connectionState = ConnectionState.Unavailable
            }
        }

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        // Check initial state
        val isConnected = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
            ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) ?: false
        connectionState = if (isConnected) ConnectionState.Available else ConnectionState.Unavailable

        connectivityManager.registerNetworkCallback(networkRequest, callback)

        onDispose {
            connectivityManager.unregisterNetworkCallback(callback)
        }
    }

    return connectionState
}
