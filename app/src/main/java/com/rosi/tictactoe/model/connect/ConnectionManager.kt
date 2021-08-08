package com.rosi.tictactoe.model.connect

import com.rosi.tictactoe.*
import com.rosi.tictactoe.base.logging.Logger
import com.rosi.tictactoe.base.actor.Actor
import com.rosi.tictactoe.base.actor.Message
import com.rosi.tictactoe.base.actor.send
import com.rosi.tictactoe.controller.Controller
import com.sr01.p2p.identity.NameIdentity
import kotlinx.coroutines.CoroutineScope

class ConnectionManager(private val controller: Controller, name: String, logger: Logger, scope: CoroutineScope) : Actor(name, logger, scope) {

    private val tag = "ConnectionManager"
    private val discoveredUsersMap = mutableMapOf<String, User>()
    private var connectionState: ConnectionState<User> = ConnectionState.Disconnected()

    override suspend fun receive(message: Message) {
        super.receive(message)

        when (message) {
            is StartDiscoverMessage -> startDiscoverMessage(message, this.connectionState)
            is StopDiscoverMessage -> stopDiscoverMessage(message)
            is UserAvailableMessage -> userAvailableMessage(message, message.identity)
            is UserUnavailableMessage -> userUnavailableMessage(message, message.identity)
            is ConnectMessage -> connectMessage(message, message.user, this.connectionState)
            is DisconnectMessage -> disconnectMessage(message, message.user)
            is ConnectedMessage -> connectedMessage(message, message.address)
            is IncomingCallMessage -> incomingCallMessage(message, message.user)
            is FailedToConnectMessage -> failedToConnectMessage(message, message.address)
            is DisconnectedMessage -> disconnectedMessage(message, message.address)
            is AcceptCallMessage -> acceptCallMessage(message, message.user, this.connectionState)
            is RejectCallMessage -> rejectCallMessage(message, message.user)
            is ConnectionAccepted -> connectionAccepted(message, message.address, this.connectionState)
            is ConnectionRejected -> connectionRejected(message, message.address, this.connectionState)
        }
    }

    private suspend fun startDiscoverMessage(message: StartDiscoverMessage, connectionState: ConnectionState<User>) {
        this.discoveredUsersMap.clear()

        when (connectionState) {
            is ConnectionState.Connected -> {
                val user = connectionState.destination
                this.discoveredUsersMap[user.address] = user
                val updatedMessage = message.withConnectedUser(user)
                this send updatedMessage to controller
            }
            is ConnectionState.Connecting -> {
                val user = connectionState.destination
                this.discoveredUsersMap[user.address] = user
                val updatedMessage = message.withConnectedUser(user)
                this send updatedMessage to controller
            }
            else -> {
                this send message to controller
            }
        }
    }

    private suspend fun stopDiscoverMessage(message: StopDiscoverMessage) {
        this send message to controller
    }

    private suspend fun userAvailableMessage(message: UserAvailableMessage, identity: NameIdentity) {
        val user = User(identity.name, identity.host, identity.port)
        if (!discoveredUsersMap.contains(user.address)) {
            discoveredUsersMap[user.address] = user
            this send message.withUser(user) to controller
        }
    }

    private fun userUnavailableMessage(message: UserUnavailableMessage, identity: NameIdentity) {
        val user = User(identity.name, identity.host, identity.port)
        if (discoveredUsersMap.remove(user.address) != null) {
            this send message.withUser(user) to controller
        }
    }


    private suspend fun connectMessage(connectMessage: ConnectMessage, user: User, connectionState: ConnectionState<User>) {
        logger.testPrint(tag, "connectMessage, $connectMessage, connectionState: $connectionState")

        when (connectionState) {
            is ConnectionState.Disconnected -> {
                this.connectionState = ConnectionState.Connecting(user)
                val updatedUser = user.withConnectionStatus(UserConnectionState.Connecting)
                this.discoveredUsersMap[user.address] = updatedUser
                this send connectMessage.withAddress(updatedUser.address).withPort(updatedUser.port) to controller
                this send ConnectingMessage(user = updatedUser) to controller
            }
            is ConnectionState.Connecting -> {
                logger.d(tag, "disconnect pending connection user: ${connectionState.destination}, before connect to: $user, and re-schedule connect")
                this send DisconnectMessage(user = connectionState.destination, address = connectionState.destination.address) to controller
            }
            is ConnectionState.Connected -> {
                logger.d(tag, "disconnect pending connection user: ${connectionState.destination}, before connect to: $user, and re-schedule connect")
                this send DisconnectMessage(user = connectionState.destination, address = connectionState.destination.address) to controller
            }
        }
    }

    private suspend fun disconnectMessage(disconnectMessage: DisconnectMessage, from: User) {
        logger.testPrint(tag, "disconnectMessage, ")

        connectionState.let { connectionState ->
            when (connectionState) {
                is ConnectionState.Connecting -> {
                    if (connectionState.destination == from) {
                        this send disconnectMessage.withAddress(from.address) to controller
                    }
                }
                is ConnectionState.Connected -> {
                    if (connectionState.destination == from) {
                        this send disconnectMessage.withAddress(from.address) to controller
                    } else {
                        logger.e(tag, "received DisconnectMessage: $disconnectMessage, with a different user then connected: ${connectionState.destination}")
                    }
                }
            }
        }
    }

    private suspend fun connectedMessage(connectedMessage: ConnectedMessage, address: String) {

        var user = getOrCreateUserByAddress(address)

        connectionState.let { connectionState ->
            when (connectionState) {
                is ConnectionState.Connecting -> {
                    if (connectionState.destination == user) {
                        val updatedUser = user.withConnectionStatus(UserConnectionState.AwaitForApproval)
                        this.discoveredUsersMap[user.address] = updatedUser
                        this.connectionState = ConnectionState.Connected(updatedUser)
                        this send connectedMessage.withUser(updatedUser) to controller
                    } else {
                        logger.e(tag, "received ConnectedMessage to: $connectedMessage, with different address then pending connect user: ${connectionState.destination}")
                    }
                }
            }
        }
    }

    private suspend fun failedToConnectMessage(failedToConnectMessage: FailedToConnectMessage, address: String) {
        getOrCreateUserByAddress(address)?.let { user ->
            connectionState.let { connectionState ->
                when (connectionState) {
                    is ConnectionState.Connecting -> {
                        if (connectionState.destination == user) {
                            this.connectionState = ConnectionState.Disconnected()
                            val updatedUser = user.withConnectionStatus(UserConnectionState.FailedToConnect)
                            this.discoveredUsersMap[user.address] = updatedUser
                            this send failedToConnectMessage.withUser(updatedUser) to controller
                        } else {
                            logger.e(
                                tag,
                                "received FailedToConnectMessage: $failedToConnectMessage, with different address then pending connect user: ${connectionState.destination}"
                            )
                        }
                    }
                }
            }
        }
    }

    private suspend fun disconnectedMessage(disconnectedMessage: DisconnectedMessage, address: String) {
        val user = getOrCreateUserByAddress(address)
        logger.testPrint(tag, "disconnectedMessage, user: $user")

        connectionState.let { connectionState ->
            when (connectionState) {
                is ConnectionState.Disconnected -> {
                }
                is ConnectionState.Connecting -> {
                    if (connectionState.destination.address == address) {
                        this.connectionState = ConnectionState.Disconnected()
                    }
                }
                is ConnectionState.Connected -> {
                    if (connectionState.destination.address == address) {
                        this.connectionState = ConnectionState.Disconnected()
                    }
                }
            }
        }

        val updatedUser = user.withConnectionStatus(UserConnectionState.Disconnected)
        this.discoveredUsersMap[user.address] = updatedUser
        this send disconnectedMessage.withUser(updatedUser) to controller
    }

    private suspend fun incomingCallMessage(message: IncomingCallMessage, user: User) {
        val updatedUser = user.withConnectionStatus(UserConnectionState.AwaitForApproval)
        this.discoveredUsersMap[updatedUser.address] = updatedUser
        this send message.withUser(updatedUser) to controller
    }

    private suspend fun acceptCallMessage(message: AcceptCallMessage, user: User, connectionState: ConnectionState<User>) {
        when (connectionState) {
            is ConnectionState.Connecting -> {
                logger.d(tag, "disconnect pending connection: ${connectionState.destination}, before accept connection from: $user")
                this send DisconnectMessage(user = connectionState.destination, address = connectionState.destination.address) to controller
            }
            is ConnectionState.Connected -> {
                logger.d(tag, "disconnect connected connection: ${connectionState.destination}, before accept connection from: $user")
                this send DisconnectMessage(user = connectionState.destination, address = connectionState.destination.address) to controller
            }
        }

        val updatedUser = user.withConnectionStatus(UserConnectionState.Connected)
        this.discoveredUsersMap[updatedUser.address] = updatedUser
        this.connectionState = ConnectionState.Connected(updatedUser)
        this send message.withUser(updatedUser).withAddress(updatedUser.address) to controller
    }

    private suspend fun rejectCallMessage(message: RejectCallMessage, user: User) {
        logger.testPrint(tag, "rejectCallMessage, connectionState: $connectionState")
        this send message.withAddress(user.address) to controller
    }

    private suspend fun connectionAccepted(message: ConnectionAccepted, address: String, connectionState: ConnectionState<User>) {
        when (connectionState) {
            is ConnectionState.Connected -> {
                if (connectionState.destination.address == address) {
                    val user = connectionState.destination
                    val updatedUser = user.withConnectionStatus(UserConnectionState.Connected)
                    this.connectionState = ConnectionState.Connected(destination = updatedUser)
                    this.discoveredUsersMap[user.address] = updatedUser
                    this send message.withUser(updatedUser) to controller
                } else {
                    logger.e(tag, "received ConnectionAccepted: $message, with a different address then connected user: ${connectionState.destination}")
                }
            }
        }
    }

    private suspend fun connectionRejected(message: ConnectionRejected, address: String, connectionState: ConnectionState<User>) {
        when (connectionState) {
            is ConnectionState.Disconnected -> {
            }
            is ConnectionState.Connecting -> {
            }
            is ConnectionState.Connected -> {
                if (connectionState.destination.address == address) {
                    this send message.withUser(connectionState.destination) to controller
                } else {
                    logger.e(tag, "received ConnectionRejected: $message, with a different address then connected user: ${connectionState.destination}")
                }
            }
        }
    }

    private fun getOrCreateUserByAddress(address: String): User {
        var user = discoveredUsersMap[address]
        if (user == null) {
            user = User(address, address, 0)
            discoveredUsersMap[address] = user
        }
        return user
    }

    sealed class ConnectionState<TDestination> {
        class Disconnected<TDestination>() : ConnectionState<TDestination>()
        data class Connecting<TDestination>(val destination: TDestination) : ConnectionState<TDestination>()
        data class Connected<TDestination>(val destination: TDestination) : ConnectionState<TDestination>()
    }

    companion object {
        private const val SCHEDULED_CONNECT_DELAY_MILLIS = 100L
    }
}
