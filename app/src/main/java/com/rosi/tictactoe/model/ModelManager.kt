package com.rosi.tictactoe.model

import com.rosi.tictactoe.*
import com.rosi.tictactoe.base.actor.Actor
import com.rosi.tictactoe.base.actor.Message
import com.rosi.tictactoe.base.actor.send
import com.rosi.tictactoe.base.di.DependencyProvider
import com.rosi.tictactoe.controller.Controller
import com.rosi.tictactoe.model.connect.ConnectionManager
import com.rosi.tictactoe.model.connect.User
import com.rosi.tictactoe.model.game.*

class ModelManager(private val controller: Controller, override val name: String, private val deps: DependencyProvider) : Actor(name, deps.logger, deps.generalScope) {

    private var gameManager: GameManager? = null
    private val connectionManager = ConnectionManager(controller, "model-manager/connection-manager", deps.logger, deps.generalScope)
    private var gameState: GameState? = null
    private val myPlayerName
        get() = deps.settings.getPlayerName()

    override suspend fun receive(message: Message) {
        super.receive(message)

        when (message) {
            is StartDiscoverMessage -> this send message to connectionManager
            is StopDiscoverMessage -> this send message to connectionManager
            is UserAvailableMessage -> this send message to connectionManager
            is UserUnavailableMessage -> this send message to connectionManager
            is IncomingCallMessage -> this send message to connectionManager
            is ConnectMessage -> this send message to connectionManager
            is ConnectedMessage -> this send message to connectionManager
            is DisconnectMessage -> this send message to connectionManager
            is DisconnectedMessage -> {
                this send message to gameManager
                this send message to connectionManager
            }
            is FailedToConnectMessage -> this send message to connectionManager
            is AcceptCallMessage -> acceptCallMessage(message, message.user)
            is RejectCallMessage -> this send message to connectionManager
            is ConnectionAccepted -> this send message to connectionManager
            is ConnectionRejected -> this send message to connectionManager

            is GameStateUpdateMessage -> with(message) { gameStateUpdateMessage(message, gameState, gameState.player1, gameState.player2, gameState.status, gameState.winner) }
            is PlayerMoveLocalMessage -> playerMoveMessage(message)
            is PlayerMoveRemoteMessage -> this send message to gameManager
            is GetGameStateMessage -> getGameStateMessage(message)
            is SetPlayerNameMessage -> setPlayerNameMessage(message, message.playerName)
            is GetPlayerNameMessage -> getPlayerNameMessage(message)
            is ExitGameMessage -> exitGameMessage(message)
            is PlayAgainRequestLocalMessage -> playAgainRequestLocalMessage(message)
            is PlayAgainRequestRemoteMessage -> playAgainRequestRemoteMessage(message)
            is PlayAgainResponseLocalMessage -> playAgainResponseLocalMessage(message)
            is PlayAgainResponseRemoteMessage -> playAgainResponseRemoteMessage(message)
        }
    }

    private fun gameStateUpdateMessage(message: GameStateUpdateMessage, gameState: GameState, player1: Player, player2: Player, gameStatus: GameStatus, winner: Player) {
        val myPlayer = listOf(player1, player2).firstOrNull { it.name == myPlayerName }
        val otherPlayer = if (myPlayer == player1) player2 else player1

        myPlayer?.let { myPlayer ->

            val win = when (gameStatus) {
                GameStatus.End -> {
                    when (winner) {
                        Player.none -> Win.Draw
                        myPlayer -> Win.My(myPlayer)
                        else -> Win.Other(otherPlayer)
                    }
                }
                else -> null
            }

            val updatedGameState = gameState.copy(myPlayer = myPlayer, otherPlayer = otherPlayer, win = win)

            this.gameState = updatedGameState

            this send message.withGameState(updatedGameState) to controller
        }
    }

    private fun acceptCallMessage(message: AcceptCallMessage, otherUser: User) {
        this send message to connectionManager
        //NOTE: S.R - start a new game, set self as Player1 and caller as Player2
        gameManager = GameManager(controller, this, "model-manager/game-server", deps.logger, deps.generalScope)
        this send JoinInMessage(player = Player(myPlayerName)) to gameManager
        this send JoinInMessage(player = Player(otherUser.name)) to gameManager
    }

    private fun playerMoveMessage(message: PlayerMoveLocalMessage) {
        gameState?.myPlayer?.let { myPlayer ->
            if (gameManager != null) {
                this send message.withPlayer(myPlayer) to gameManager
            } else {
                this send message.withPlayer(myPlayer) to controller
            }
        }
    }

    private fun getGameStateMessage(message: GetGameStateMessage) {
        gameState?.let { gameState ->
            this send message.withGameState(gameState) to controller
        }
    }

    private fun setPlayerNameMessage(message: SetPlayerNameMessage, playerName: String) {
        deps.settings.setPlayerName(playerName)
        this send message to controller
    }

    private fun getPlayerNameMessage(message: GetPlayerNameMessage) {
        this send message.withPlayerName(myPlayerName) to controller
    }

    private fun exitGameMessage(message: ExitGameMessage) {
        if (gameManager != null) {
            this send message to gameManager
        } else {
            this send message to controller
        }
    }

    private fun playAgainRequestLocalMessage(message: PlayAgainRequestLocalMessage) {
        gameState?.let {
            val updatedMessage = message.withRequestBy(it.myPlayer)
            this send updatedMessage to controller
        }
    }

    private fun playAgainRequestRemoteMessage(message: PlayAgainRequestRemoteMessage) {
        this send message to controller
    }

    private fun playAgainResponseLocalMessage(message: PlayAgainResponseLocalMessage) {
        gameState?.let {
            val updatedMessage = message.withAnsweredBy(it.myPlayer)
            this send updatedMessage to controller
            this send updatedMessage to gameManager
        }
    }

    private fun playAgainResponseRemoteMessage(message: PlayAgainResponseRemoteMessage) {
        this send message to gameManager
        this send message to controller
    }
}