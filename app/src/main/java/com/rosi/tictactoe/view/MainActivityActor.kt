package com.rosi.tictactoe.view

import com.rosi.tictactoe.*
import com.rosi.tictactoe.base.actor.Actor
import com.rosi.tictactoe.base.actor.Message
import com.rosi.tictactoe.base.actor.send
import com.rosi.tictactoe.base.logging.Logger
import com.rosi.tictactoe.controller.Controller
import com.rosi.tictactoe.model.connect.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.onSubscription

class MainActivityActor(private val controller: Controller, private val viewManager: ViewManager, name: String, logger: Logger, scope: CoroutineScope) :
    Actor(name, logger, scope) {

    private val sharedFlow = MutableSharedFlow<GameUIEvent>(replay = 0)
    val eventsFlow: SharedFlow<GameUIEvent> = sharedFlow

    override suspend fun receive(message: Message) {
        super.receive(message)

        when (message) {
            is SetPlayerNameMessage -> sharedFlow.emit(GameUIEvent.PlayerNameUIEvent(message.playerName))
            is GetPlayerNameMessage -> if (message.playerName != null) sharedFlow.emit(GameUIEvent.PlayerNameUIEvent(message.playerName))
            is UserAvailableMessage -> if (message.user != null) sharedFlow.emit(GameUIEvent.UserAvailableUIEvent(message.user))
            is UserUnavailableMessage -> if (message.user != null) sharedFlow.emit(GameUIEvent.UserUnavailableUIEvent(message.user))
            is ConnectingMessage -> sharedFlow.emit(GameUIEvent.ConnectingUIEvent(message.user))
            is ConnectedMessage -> if (message.user != null) sharedFlow.emit(GameUIEvent.ConnectedUIEvent(message.user))
            is FailedToConnectMessage -> if (message.user != null) sharedFlow.emit(GameUIEvent.FailedToConnectUIEvent(message.user))
            is ConnectionAccepted -> if (message.user != null) sharedFlow.emit(GameUIEvent.ConnectionAcceptedUIEvent(message.user))
            is ConnectionRejected -> if (message.user != null) sharedFlow.emit(GameUIEvent.ConnectionRejectedUIEvent(message.user))
            is DisconnectedMessage -> if (message.user != null) sharedFlow.emit(GameUIEvent.DisconnectedUIEvent(message.user))
            is AcceptCallMessage -> sharedFlow.emit(GameUIEvent.ConnectionAcceptedUIEvent(message.user))
            is IncomingCallMessage -> sharedFlow.emit(GameUIEvent.IncomingCallUIEvent(message.user))
            is GameStateUpdateMessage -> sharedFlow.emit(GameUIEvent.GameStatusUIEvent(message.gameState))
            is GetGameStateMessage -> if (message.gameState != null) sharedFlow.emit(GameUIEvent.GameStatusUIEvent(message.gameState))
            is StartDiscoverMessage -> if (message.connectedUser != null) sharedFlow.emit(GameUIEvent.UserAvailableUIEvent(message.connectedUser))
            is PlayAgainRequestRemoteMessage -> if (message.requestBy != null) sharedFlow.emit(GameUIEvent.PlayAgainRequestUIEvent(message.requestBy))
            is PlayAgainResponseRemoteMessage -> if (message.answeredBy != null) sharedFlow.emit(GameUIEvent.PlayAgainResponseUIEvent(message.answeredBy, message.isAccepted))
        }
    }

    fun getEventFlow(): Flow<GameUIEvent> {
        return eventsFlow
            .onSubscription {
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

