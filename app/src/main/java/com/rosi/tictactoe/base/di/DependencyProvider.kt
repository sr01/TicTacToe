package com.rosi.tictactoe.base.di

import com.rosi.tictactoe.base.logging.Logger
import com.rosi.tictactoe.controller.Controller
import com.rosi.tictactoe.model.settings.Settings
import com.rosi.tictactoe.view.com.GameComMessage
import com.rosi.tictactoe.view.com.TcpPeerFactory
import com.sr01.p2p.discovery.NetworkDiscoveryService
import com.sr01.p2p.identity.IdentityProvider
import com.sr01.p2p.identity.NameIdentity
import kotlinx.coroutines.CoroutineScope

interface DependencyProvider {

    val logger: Logger

    val settings : Settings

    val nds: NetworkDiscoveryService<NameIdentity>

    val identityProvider: IdentityProvider<NameIdentity>

    val mainScope: CoroutineScope

    val ioScope: CoroutineScope

    val generalScope: CoroutineScope

    val controller: Controller

    val peerFactory : TcpPeerFactory<GameComMessage>
}