package com.rosi.tictactoe.view.com

import com.rosi.tictactoe.*
import com.rosi.tictactoe.base.logging.Logger
import com.rosi.tictactoe.base.actor.*
import com.rosi.tictactoe.controller.Controller
import com.rosi.tictactoe.model.connect.User
import com.rosi.tictactoe.model.game.Cell
import com.rosi.tictactoe.model.game.GameState
import com.rosi.tictactoe.model.game.GameStatus
import com.rosi.tictactoe.model.game.Player
import com.rosi.tictactoe.view.ViewManager
import com.sr01.p2p.identity.IdentityProvider
import com.sr01.p2p.identity.NameIdentity
import com.sr01.p2p.peer.Peer
import com.sr01.p2p.peer.PeerConnection
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TcpGameCommService(private val controller: Controller, private val viewManager: ViewManager, private val identityProvider: IdentityProvider<NameIdentity>,
    private val peerFactory: TcpPeerFactory<GameComMessage>, name: String, logger: Logger, scope: CoroutineScope) : Actor(name, logger, scope) {

    private lateinit var peer: Peer<GameComMessage>
    private var outgoingConnection: PeerConnection<GameComMessage>? = null
    private var incomingConnection: PeerConnection<GameComMessage>? = null
    private var activeConnection: PeerConnection<GameComMessage>? = null
    private val scope = CoroutineScope(Dispatchers.Default)

    override suspend fun receive(message: Message) {
        when (message) {
            is AppStartMessage -> appStartMessage()
            is AppStopMessage -> appStopMessage()

            is ConnectMessage -> if (message.address != null) connectMessage(message.address, message.port)
            is DisconnectMessage -> if (message.address != null) disconnectMessage(message, message.address)
            is AcceptCallMessage -> if (message.address != null) acceptCallMessage(message, message.address)
            is RejectCallMessage -> if (message.address != null) rejectCallMessage(message, message.address)
            is FailedToConnectMessage -> failedToConnectMessage(message)
            is DisconnectedMessage -> disconnectedMessage(message)

            is InternalMessages.IncomingCallMessage -> incomingCallMessage(message.peerConnection)
            is InternalMessages.ConnectionAcceptedMessage -> connectionAcceptedMessage(message.peerConnection)
            is InternalMessages.ConnectionRejectedMessage -> connectionRejectedMessage(message.peerConnection)

            is PlayerMoveLocalMessage -> if (message.player != null) playerMoveMessage(message.player, message.x, message.y)
            is GameStateUpdateLocalMessage -> with(message.gameState) { gameStatusMessage(status, board, currentPlayer, player1, player2, winner) }
            is ExitGameMessage -> exitGameMessage()
            is PlayAgainRequestLocalMessage -> if (message.requestBy != null) playAgainRequestMessage(message.requestBy)
            is PlayAgainResponseLocalMessage -> if (message.answeredBy != null) playAgainResponseMessage(message.answeredBy, message.isAccepted)
        }
    }

    private fun appStartMessage() {
        peer = peerFactory.create().apply {
            onIncomingConnection { this@TcpGameCommService send InternalMessages.IncomingCallMessage(peerConnection = it) to this@TcpGameCommService }
            start()
        }
    }

    private fun appStopMessage() {
        peer.stop()
        outgoingConnection?.disconnect()
        incomingConnection?.disconnect()
        activeConnection?.disconnect()

        outgoingConnection = null
        incomingConnection = null
        activeConnection = null
    }

    private fun connectMessage(address: String, port: Int) {

        disconnect()

        peer.connect(address, port, onConnected = { peerConnection ->
            outgoingConnection = peerConnection
            attachConnectionEvents(peerConnection, address)
        })
    }

    private fun disconnectMessage(message: DisconnectMessage, address: String) {
        disconnect()
    }

    private fun acceptCallMessage(message: AcceptCallMessage, address: String) {
        incomingConnection?.let { peerConnection ->
            activeConnection = peerConnection
            incomingConnection = null
            peerConnection.send(GameComMessage.ConnectionActionMessage(GameComMessage.ConnectionActions.Accept))
            this send message.withHandled(true) to viewManager
        }
    }

    private fun rejectCallMessage(message: RejectCallMessage, address: String) {
        incomingConnection?.let { peerConnection ->
            peerConnection.sendAndDisconnect(GameComMessage.ConnectionActionMessage(GameComMessage.ConnectionActions.Reject))
            incomingConnection = null
        }
    }

    private fun failedToConnectMessage(message: FailedToConnectMessage) {
        outgoingConnection?.let {
            peer.disconnect(it.id)
        }
        outgoingConnection = null

        this send message to controller
    }

    private fun disconnectedMessage(message: DisconnectedMessage) {
        activeConnection = null
        this send message to controller
    }

    private fun incomingCallMessage(peerConnection: PeerConnection<GameComMessage>) {
        this.incomingConnection = peerConnection

        attachConnectionEvents(peerConnection, peerConnection.id)
    }

    private fun connectionAcceptedMessage(peerConnection: PeerConnection<GameComMessage>) {

        activeConnection = outgoingConnection
        outgoingConnection = null

        this send ConnectionAccepted(address = peerConnection.id) to controller
    }

    private fun connectionRejectedMessage(peerConnection: PeerConnection<GameComMessage>) {

        outgoingConnection?.let {
            peer.disconnect(it.id)
        }
        outgoingConnection = null

        this send ConnectionRejected(address = peerConnection.id) to controller
    }

    private fun playerMoveMessage(player: Player, x: Int, y: Int) {
        activeConnection?.send(GameComMessage.PlayerMoveMessage(player, x, y))
    }

    private fun gameStatusMessage(gameState: GameStatus, board: List<List<Cell>>, currentPlayer: Player, player1: Player, player2: Player, winner: Player) {
        activeConnection?.send(GameComMessage.GameStateUpdateMessage(gameState, board, currentPlayer, player1, player2, winner))
    }

    private fun exitGameMessage() {
        activeConnection?.disconnect()
        activeConnection = null
    }

    private fun playAgainRequestMessage(requestBy: Player) {
        activeConnection?.send(GameComMessage.PlayAgainRequestMessage(requestBy))
    }

    private fun playAgainResponseMessage(answeredBy: Player, isAccepted: Boolean) {
        activeConnection?.send(GameComMessage.PlayAgainResponseMessage(answeredBy, isAccepted))
    }

    private fun disconnect() {
        activeConnection?.let { connection ->
            peer.disconnect(connection.id)
        }
        outgoingConnection?.let { connection ->
            peer.disconnect(connection.id)
        }
        activeConnection = null
        outgoingConnection = null
    }

    private fun attachConnectionEvents(peerConnection: PeerConnection<GameComMessage>, address: String) {

        val self = this

        peerConnection.onConnected {
            scope.launch {
                self send ConnectedMessage(address = address) to controller
                outgoingConnection?.send(GameComMessage.IdentityMessage(identityProvider.get()))
            }
        }
        peerConnection.onDisconnected {
            scope.launch {
                self send DisconnectedMessage(address = address) to self
            }
        }
        peerConnection.onFailedToConnect {
            scope.launch {
                self send FailedToConnectMessage(address = address) to self
            }
        }
        peerConnection.onMessage { connection, message ->
            scope.launch {
                when (message) {
                    is GameComMessage.ConnectionActionMessage -> {
                        when (message.action) {
                            GameComMessage.ConnectionActions.Accept -> {
                                self send InternalMessages.ConnectionAcceptedMessage(peerConnection = connection) to self
                            }
                            GameComMessage.ConnectionActions.Reject -> {
                                self send InternalMessages.ConnectionRejectedMessage(peerConnection = connection) to self
                            }
                        }
                    }
                    is GameComMessage.IdentityMessage -> {
                        val identity = message.identity
                        val user = User(identity.name, identity.host, identity.port)
                        self send IncomingCallMessage(address = peerConnection.id, user = user) to controller
                    }
                    is GameComMessage.PlayerMoveMessage -> {
                        with(message) {
                            self send PlayerMoveRemoteMessage(player = player, x = x, y = y) to controller
                        }
                    }
                    is GameComMessage.GameStateUpdateMessage -> {
                        with(message) {
                            self send GameStateUpdateRemoteMessage(
                                gameState = GameState(status, board, player1, player2, currentPlayer, winner, Player.none, Player.none, null)
                            ) to controller
                        }
                    }
                    is GameComMessage.PlayAgainRequestMessage -> {
                        with(message) {
                            self send PlayAgainRequestRemoteMessage(requestBy = requestBy) to controller
                        }
                    }
                    is GameComMessage.PlayAgainResponseMessage -> {
                        with(message) {
                            self send PlayAgainResponseRemoteMessage(answeredBy = answeredBy, isAccepted = isAccepted) to controller
                        }
                    }
                }
            }
        }
    }

    private interface InternalMessages {
        data class IncomingCallMessage(override val sender: ISender = DefaultSender, val peerConnection: PeerConnection<GameComMessage>) : Message {
            override fun withSender(sender: ISender) = this.copy(sender = sender)
        }

        data class ConnectionAcceptedMessage(override val sender: ISender = DefaultSender, val peerConnection: PeerConnection<GameComMessage>) : Message {
            override fun withSender(sender: ISender) = this.copy(sender = sender)
        }

        data class ConnectionRejectedMessage(override val sender: ISender = DefaultSender, val peerConnection: PeerConnection<GameComMessage>) : Message {
            override fun withSender(sender: ISender) = this.copy(sender = sender)
        }
    }
}

sealed class GameComMessage(val messageType: MessageType) {

    data class ConnectionActionMessage(val action: ConnectionActions) : GameComMessage(MessageType.ConnectionAction)

    data class IdentityMessage(val identity: NameIdentity) : GameComMessage(MessageType.Identity)

    data class PlayerMoveMessage(val player: Player, val x: Int, val y: Int) : GameComMessage(MessageType.PlayerMove)

    data class GameStateUpdateMessage(val status: GameStatus, val board: List<List<Cell>>, val currentPlayer: Player, val player1: Player, val player2: Player,
        val winner: Player) : GameComMessage(MessageType.GameStatus)


    data class PlayAgainRequestMessage(val requestBy: Player) : GameComMessage(MessageType.PlayAgainRequest)
    data class PlayAgainResponseMessage(val answeredBy: Player, val isAccepted: Boolean) : GameComMessage(MessageType.PlayAgainResponse)

    enum class ConnectionActions { Accept, Reject }

    enum class MessageType { ConnectionAction, Identity, PlayerMove, GameStatus, PlayAgainRequest, PlayAgainResponse }
}