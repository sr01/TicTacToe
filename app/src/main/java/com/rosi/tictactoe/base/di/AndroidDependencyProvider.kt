package com.rosi.tictactoe.base.di

import android.content.Context
import android.preference.PreferenceManager
import com.rosi.tictactoe.base.logging.Logger
import com.rosi.tictactoe.controller.Controller
import com.rosi.tictactoe.model.settings.Settings
import com.rosi.tictactoe.utils.AndroidLogger
import com.rosi.tictactoe.utils.AndroidNetworkStateProvider
import com.rosi.tictactoe.utils.P2PLogger
import com.rosi.tictactoe.view.com.GameComMessage
import com.rosi.tictactoe.view.com.GameMessageJsonProtocol
import com.rosi.tictactoe.view.com.TcpPeerFactory
import com.rosi.tictactoe.view.settings.SharedPreferencesSettings
import com.sr01.p2p.discovery.NetworkDiscoveryService
import com.sr01.p2p.discovery.udp.UDPNetworkDiscoveryService
import com.sr01.p2p.identity.IdentityProvider
import com.sr01.p2p.identity.NameIdentity
import com.sr01.p2p.identity.NameIdentitySerializer
import com.sr01.p2p.peer.tcp.TcpPeer
import com.sr01.p2p.utils.IPAddressProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.MainScope

class AndroidDependencyProvider(context: Context) : DependencyProvider {

    override val logger: Logger by lazy {
        AndroidLogger
    }

    override val settings: Settings by lazy {
        SharedPreferencesSettings(context.resources, PreferenceManager.getDefaultSharedPreferences(context))
    }

    override val nds: NetworkDiscoveryService<NameIdentity> by lazy {
        UDPNetworkDiscoveryService(
            identityProvider,
            NameIdentitySerializer,
            NameIdentitySerializer,
            ipAddressProvider,
            P2PLogger,
            settings.getComPort()
        )
    }

    override val identityProvider: IdentityProvider<NameIdentity> by lazy {
        object : IdentityProvider<NameIdentity> {
            override fun get(): NameIdentity {
                val playerName = settings.getPlayerName()
                val ipAddress = ipAddressProvider.getConnectedWiFiIPAddress()
                return NameIdentity(playerName, "", ipAddress, settings.getComPort())
            }
        }
    }

    override val mainScope: CoroutineScope by lazy {
        MainScope()
    }

    override val ioScope: CoroutineScope by lazy {
        CoroutineScope(Dispatchers.IO + Job())
    }

    override val generalScope: CoroutineScope by lazy {
        CoroutineScope(Dispatchers.Default + Job())
    }

    override val controller: Controller by lazy {
        Controller(this)
    }

    val ipAddressProvider: IPAddressProvider by lazy {
        AndroidNetworkStateProvider(context)
    }

    override val peerFactory: TcpPeerFactory<GameComMessage> by lazy {
        TcpPeerFactory {
            TcpPeer(settings.getComPort(), GameMessageJsonProtocol, P2PLogger)
        }
    }
}