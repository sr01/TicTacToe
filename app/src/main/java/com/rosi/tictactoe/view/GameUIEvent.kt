package com.rosi.tictactoe.view

import com.rosi.tictactoe.model.connect.User
import com.rosi.tictactoe.model.game.GameState
import com.rosi.tictactoe.model.game.Player

sealed class GameUIEvent {
    data class PlayerNameUIEvent(val playerName: String) : GameUIEvent()
    data class UserAvailableUIEvent(val user: User) : GameUIEvent()
    data class UserUnavailableUIEvent(val user: User) : GameUIEvent()
    data class ConnectingUIEvent(val user: User) : GameUIEvent()
    data class ConnectedUIEvent(val user: User) : GameUIEvent()
    data class FailedToConnectUIEvent(val user: User) : GameUIEvent()
    data class ConnectionAcceptedUIEvent(val user: User) : GameUIEvent()
    data class ConnectionRejectedUIEvent(val user: User) : GameUIEvent()
    data class DisconnectedUIEvent(val user: User) : GameUIEvent()
    data class IncomingCallUIEvent(val user: User) : GameUIEvent()
    data class GameStatusUIEvent(val state: GameState) : GameUIEvent()
    data class PlayAgainRequestUIEvent(val requestBy: Player) : GameUIEvent()
    data class PlayAgainResponseUIEvent(val answeredBy: Player, val isAccepted: Boolean) : GameUIEvent()

    object NavigateToPlayersUIEvent : GameUIEvent()
    object InvalidPlayerNameUIEvent : GameUIEvent()
}