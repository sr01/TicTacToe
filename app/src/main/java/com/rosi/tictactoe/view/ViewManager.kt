package com.rosi.tictactoe.view

import com.rosi.tictactoe.*
import com.rosi.tictactoe.base.actor.Actor
import com.rosi.tictactoe.base.actor.Message
import com.rosi.tictactoe.base.actor.send
import com.rosi.tictactoe.base.di.DependencyProvider
import com.rosi.tictactoe.controller.Controller
import com.rosi.tictactoe.view.com.TcpGameCommService
import com.sr01.p2p.discovery.DiscoveryListener
import com.sr01.p2p.identity.NameIdentity

class ViewManager(controller: Controller, override val name: String, deps: DependencyProvider) : Actor(name, deps.logger, deps.generalScope) {

    private val nds = deps.nds

    val mainActivityActor = MainActivityActor(controller, this, "view-manager/main-activity-actor", logger, deps.generalScope)
    private val gameComService = TcpGameCommService(controller, this, deps.identityProvider, deps.peerFactory, "view-manager/game-com-service", logger, deps.ioScope)

    init {
        val self = this

        nds.setDiscoveryListener(object : DiscoveryListener<NameIdentity> {
            override fun onServerDiscovered(identity: NameIdentity) {
                self send UserAvailableMessage(identity = identity) to controller
            }

            override fun onServerLeaved(identity: NameIdentity) {
                self send UserUnavailableMessage(identity = identity) to controller
            }
        })
    }

    override suspend fun receive(message: Message) {
        super.receive(message)

        when (message) {
            is AppStartMessage -> this send message to gameComService
            is AppStopMessage -> {
                nds.stopDiscover()
                this send message to gameComService
            }
            is AppResumeMessage -> nds.startDiscoverable()
            is AppPauseMessage -> nds.stopDiscoverable()
            is StartDiscoverMessage -> {
                nds.startDiscover()
                this send message to mainActivityActor
            }
            is StopDiscoverMessage -> nds.stopDiscover()
            is UserAvailableMessage -> this send message to mainActivityActor
            is UserUnavailableMessage -> this send message to mainActivityActor
            is ConnectMessage -> this send message to gameComService
            is DisconnectMessage -> this send message to gameComService
            is ConnectingMessage -> this send message to mainActivityActor
            is ConnectedMessage -> this send message to mainActivityActor
            is FailedToConnectMessage -> this send message to mainActivityActor
            is DisconnectedMessage -> this send message to mainActivityActor
            is IncomingCallMessage -> this send message to mainActivityActor
            is AcceptCallMessage -> when (message.isHandled) {
                false -> this send message to gameComService
                true -> this send message to mainActivityActor
            }
            is RejectCallMessage -> this send message to gameComService
            is ConnectionAccepted -> this send message to mainActivityActor
            is ConnectionRejected -> this send message to mainActivityActor

            is PlayerMoveLocalMessage -> this send message to gameComService
            is GameStateUpdateLocalMessage -> {
                this send message to gameComService
                this send message to mainActivityActor
            }
            is GameStateUpdateRemoteMessage -> this send message to mainActivityActor
            is GetGameStateMessage -> this send message to mainActivityActor
            is GetPlayerNameMessage -> this send message to mainActivityActor
            is ExitGameMessage -> this send message to gameComService

            is PlayAgainRequestLocalMessage -> this send message to gameComService
            is PlayAgainRequestRemoteMessage -> this send message to mainActivityActor
            is PlayAgainResponseLocalMessage -> this send message to gameComService
            is PlayAgainResponseRemoteMessage -> this send message to mainActivityActor
        }
    }
}
