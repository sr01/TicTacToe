package com.rosi.tictactoe.view

import com.rosi.tictactoe.*
import com.rosi.tictactoe.base.logging.Logger
import com.rosi.tictactoe.base.actor.Actor
import com.rosi.tictactoe.base.actor.Message
import com.rosi.tictactoe.base.actor.send
import com.rosi.tictactoe.controller.Controller
import com.rosi.tictactoe.model.connect.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.BroadcastChannel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.onStart

class MainActivityActor(private val controller: Controller, private val viewManager: ViewManager, name: String, logger: Logger, scope: CoroutineScope) :
    Actor(name, logger, scope) {

    private val channel = BroadcastChannel<GameUIEvent>(1)

    override suspend fun receive(message: Message) {
        super.receive(message)

        when (message) {
            is SetPlayerNameMessage -> channel.send(GameUIEvent.PlayerNameUIEvent(message.playerName))
            is GetPlayerNameMessage -> if (message.playerName != null) channel.send(GameUIEvent.PlayerNameUIEvent(message.playerName))
            is UserAvailableMessage -> if (message.user != null) channel.send(GameUIEvent.UserAvailableUIEvent(message.user))
            is UserUnavailableMessage -> if (message.user != null) channel.send(GameUIEvent.UserUnavailableUIEvent(message.user))
            is ConnectingMessage -> channel.send(GameUIEvent.ConnectingUIEvent(message.user))
            is ConnectedMessage -> if (message.user != null) channel.send(GameUIEvent.ConnectedUIEvent(message.user))
            is FailedToConnectMessage -> if (message.user != null) channel.send(GameUIEvent.FailedToConnectUIEvent(message.user))
            is ConnectionAccepted -> if (message.user != null) channel.send(GameUIEvent.ConnectionAcceptedUIEvent(message.user))
            is ConnectionRejected -> if (message.user != null) channel.send(GameUIEvent.ConnectionRejectedUIEvent(message.user))
            is DisconnectedMessage -> if (message.user != null) channel.send(GameUIEvent.DisconnectedUIEvent(message.user))
            is AcceptCallMessage -> channel.send(GameUIEvent.ConnectionAcceptedUIEvent(message.user))
            is IncomingCallMessage -> channel.send(GameUIEvent.IncomingCallUIEvent(message.user))
            is GameStateUpdateMessage -> channel.send(GameUIEvent.GameStatusUIEvent(message.gameState))
            is GetGameStateMessage -> if (message.gameState != null) channel.send(GameUIEvent.GameStatusUIEvent(message.gameState))
            is StartDiscoverMessage -> if (message.connectedUser != null) channel.send(GameUIEvent.UserAvailableUIEvent(message.connectedUser))
            is PlayAgainRequestRemoteMessage -> if (message.requestBy != null) channel.send(GameUIEvent.PlayAgainRequestUIEvent(message.requestBy))
            is PlayAgainResponseRemoteMessage -> if (message.answeredBy != null) channel.send(GameUIEvent.PlayAgainResponseUIEvent(message.answeredBy, message.isAccepted))
        }
    }

    fun getEventFlow(): Flow<GameUIEvent> {
        return channel.asFlow().onStart {
            this@MainActivityActor send GetPlayerNameMessage() to controller
            this@MainActivityActor send GetGameStateMessage() to controller
        }
    }

    fun appStart() = this send AppStartMessage() to viewManager

    fun appStop() = this send AppStopMessage() to viewManager

    fun appPause() = this send AppPauseMessage() to viewManager

    fun appResume() = this send AppResumeMessage() to viewManager

    fun setPlayerName(playerName: String) = this send SetPlayerNameMessage(playerName = playerName) to controller

    fun startDiscover() = this send StartDiscoverMessage() to controller

    fun stopDiscover() = this send StopDiscoverMessage() to controller

    fun connectToUser(user: User) = this send ConnectMessage(user = user) to controller

    fun acceptCall(user: User) = this send AcceptCallMessage(user = user) to controller

    fun rejectCall(user: User) = this send RejectCallMessage(user = user) to controller

    fun makeGameMove(x: Int, y: Int) = this send PlayerMoveLocalMessage(x = x, y = y) to controller

    fun exitGame() {
        this send ExitGameMessage() to controller
    }

    fun playAgain() {
        this send PlayAgainRequestLocalMessage() to controller
    }

    fun acceptPlayAgain() {
        this send PlayAgainResponseLocalMessage(isAccepted = true) to controller
    }

    fun declinePlayAgain() {
        this send PlayAgainResponseLocalMessage(isAccepted = false) to controller
    }
}

