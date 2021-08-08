/*
 * Copyright (c) 2018. ROSIAPPS ALL RIGHTS RESERVED
 */

package com.rosi.tictactoe.utils

import android.content.Context
import android.content.Context.WIFI_SERVICE
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.sr01.p2p.utils.IPAddressProvider
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*

class AndroidNetworkStateProvider(private val context: Context) : IPAddressProvider {

    private val useIPv4: Boolean = true

    override fun getConnectedWiFiIPAddress(): String {
        val wifiManager = context.applicationContext.getSystemService(WIFI_SERVICE) as WifiManager
        val ipAddress = wifiManager.connectionInfo.ipAddress
        return String.format(Locale.getDefault(), "%d.%d.%d.%d",
            ipAddress and 0xff,
            ipAddress shr 8 and 0xff,
            ipAddress shr 16 and 0xff,
            ipAddress shr 24 and 0xff)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun isConnectedToLan2(): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = cm.activeNetwork

        Log.d("isConnectedToLan2", "activeNetwork: \n  $network")

        return when {
            network != null -> {
                val caps = cm.getNetworkCapabilities(network)
                when {
                    caps != null -> caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                            caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) ||
                            caps.hasTransport(NetworkCapabilities.TRANSPORT_VPN)
                    else -> false
                }
            }
            else -> false
        }
    }

    /**
     * Get IP address from first non-localhost interface
     * @param useIPv4   true=return ipv4, false=return ipv6
     * @return  address or empty string
     */
    override fun getAllIPAddresses(): List<String> =
        Collections.list(NetworkInterface.getNetworkInterfaces()).map { networkInterface ->
            val addresses: List<InetAddress> = Collections.list(networkInterface.inetAddresses)
            addresses.filter { !it.isLoopbackAddress }
                .mapNotNull {
                    val addressString: String = it.hostAddress
                    val isIPv4 = addressString.indexOf(':') < 0
                    when {
                        useIPv4 -> if (isIPv4) addressString else null
                        else -> if (!isIPv4) getIPv6Address(addressString) else null
                    }
                }
        }.flatten()

    private fun getIPv6Address(addressString: String): String {
        val delimiter = addressString.indexOf('%') // drop ip6 zone suffix
        return if (delimiter < 0) addressString.toUpperCase()
        else addressString.substring(0, delimiter).toUpperCase()
    }
}

