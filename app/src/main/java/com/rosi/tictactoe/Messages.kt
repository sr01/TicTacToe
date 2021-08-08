package com.rosi.tictactoe

import com.rosi.tictactoe.base.actor.DefaultSender
import com.rosi.tictactoe.base.actor.ISender
import com.rosi.tictactoe.base.actor.Message
import com.rosi.tictactoe.model.connect.User
import com.rosi.tictactoe.model.game.GameState
import com.rosi.tictactoe.model.game.Player
import com.sr01.p2p.identity.NameIdentity

data class AppStartMessage(override val sender: ISender = DefaultSender) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
}

data class AppStopMessage(override val sender: ISender = DefaultSender) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
}

data class AppPauseMessage(override val sender: ISender = DefaultSender) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
}

data class AppResumeMessage(override val sender: ISender = DefaultSender) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
}

// Com
data class UserAvailableMessage(override val sender: ISender = DefaultSender, val identity: NameIdentity, val user: User? = null) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
    fun withUser(user: User) = this.copy(user = user)
}

data class UserUnavailableMessage(override val sender: ISender = DefaultSender, val identity: NameIdentity, val user: User? = null) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
    fun withUser(user: User) = this.copy(user = user)
}

interface PlayerNameMessage : Message {
    val playerName: String?
}

data class SetPlayerNameMessage(override val sender: ISender = DefaultSender, override val playerName: String) : PlayerNameMessage {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
}

data class GetPlayerNameMessage(override val sender: ISender = DefaultSender, override val playerName: String? = null) : PlayerNameMessage {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
    fun withPlayerName(playerName: String) = this.copy(playerName = playerName)
}

data class StartDiscoverMessage(override val sender: ISender = DefaultSender, val connectedUser: User? = null) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
    fun withConnectedUser(connectedUser: User) = this.copy(connectedUser = connectedUser)
}

data class StopDiscoverMessage(override val sender: ISender = DefaultSender) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
}

data class ConnectMessage(override val sender: ISender = DefaultSender, val user: User, val address: String? = null, val port: Int = 0) :
    Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
    fun withAddress(address: String) = this.copy(address = address)
    fun withPort(port: Int) = this.copy(port = port)
}

data class DisconnectMessage(override val sender: ISender = DefaultSender, val user: User, val address: String? = null) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
    fun withAddress(address: String) = this.copy(address = address)
}

data class ConnectingMessage(override val sender: ISender = DefaultSender, val user: User) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
}

data class ConnectedMessage(override val sender: ISender = DefaultSender, val user: User? = null, val address: String) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
    fun withUser(user: User) = this.copy(user = user)
}

data class FailedToConnectMessage(override val sender: ISender = DefaultSender, val user: User? = null, val address: String) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
    fun withUser(user: User) = this.copy(user = user)
}

data class DisconnectedMessage(override val sender: ISender = DefaultSender, val user: User? = null, val address: String) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
    fun withUser(user: User) = this.copy(user = user)
}

data class IncomingCallMessage(override val sender: ISender = DefaultSender, val user: User, val address: String) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
    fun withUser(user: User) = this.copy(user = user)
}

data class ConnectionAccepted(override val sender: ISender = DefaultSender, val user: User? = null, val address: String) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
    fun withUser(user: User) = this.copy(user = user)
}

data class ConnectionRejected(override val sender: ISender = DefaultSender, val user: User? = null, val address: String) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
    fun withUser(user: User) = this.copy(user = user)
}

data class AcceptCallMessage(override val sender: ISender = DefaultSender, val user: User, val address: String? = null, val isHandled: Boolean = false) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
    fun withHandled(isHandled: Boolean) = this.copy(isHandled = isHandled)
    fun withUser(user: User) = this.copy(user = user)
    fun withAddress(address: String) = this.copy(address = address)
}

data class RejectCallMessage(override val sender: ISender = DefaultSender, val user: User, val address: String? = null) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
    fun withAddress(address: String) = this.copy(address = address)
}

// Game Messages
data class JoinInMessage(override val sender: ISender = DefaultSender, val player: Player) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
}

interface PlayerMoveMessage : Message {
    val player: Player?
    val x: Int
    val y: Int
}

data class PlayerMoveLocalMessage(override val sender: ISender = DefaultSender, override val player: Player? = null, override val x: Int, override val y: Int) : PlayerMoveMessage {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
    fun withPlayer(player: Player) = this.copy(player = player)
}

data class PlayerMoveRemoteMessage(override val sender: ISender = DefaultSender, override val player: Player, override val x: Int, override val y: Int) : PlayerMoveMessage {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
}

interface GameStateUpdateMessage : Message {
    val gameState: GameState

    fun withGameState(gameState: GameState): GameStateUpdateMessage
}

data class GameStateUpdateLocalMessage(override val sender: ISender = DefaultSender, override val gameState: GameState) : GameStateUpdateMessage {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
    override fun withGameState(gameState: GameState) = this.copy(gameState = gameState)
}

data class GameStateUpdateRemoteMessage(override val sender: ISender = DefaultSender, override val gameState: GameState) : GameStateUpdateMessage {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
    override fun withGameState(gameState: GameState) = this.copy(gameState = gameState)
}

data class GetGameStateMessage(override val sender: ISender = DefaultSender, val gameState: GameState? = null) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
    fun withGameState(gameState: GameState) = this.copy(gameState = gameState)
}

data class ExitGameMessage(override val sender: ISender = DefaultSender) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
}

data class PlayAgainRequestLocalMessage(override val sender: ISender = DefaultSender, val requestBy: Player? = null) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
    fun withRequestBy(requestBy: Player) = this.copy(requestBy = requestBy)
}

data class PlayAgainRequestRemoteMessage(override val sender: ISender = DefaultSender, val requestBy: Player? = null) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
    fun withRequestBy(requestBy: Player) = this.copy(requestBy = requestBy)
}

data class PlayAgainResponseLocalMessage(override val sender: ISender = DefaultSender, val answeredBy: Player? = null, val isAccepted : Boolean) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
    fun withAnsweredBy(answeredBy: Player) = this.copy(answeredBy = answeredBy)
}

data class PlayAgainResponseRemoteMessage(override val sender: ISender = DefaultSender, val answeredBy: Player? = null, val isAccepted : Boolean) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
    fun withAnsweredBy(answeredBy: Player) = this.copy(answeredBy = answeredBy)
}
