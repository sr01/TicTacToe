package com.rosi.tictactoe.model.game

import com.rosi.tictactoe.*
import com.rosi.tictactoe.base.logging.Logger
import com.rosi.tictactoe.base.actor.Actor
import com.rosi.tictactoe.base.actor.ISender
import com.rosi.tictactoe.base.actor.Message
import com.rosi.tictactoe.base.actor.send
import kotlinx.coroutines.CoroutineScope

class GameManager(private val controller: ISender, private val modelManager: ISender, name: String, logger: Logger, scope: CoroutineScope) : Actor(name, logger, scope) {

    private val tag = "GameServerActor"
    private var game: Game? = null

    override suspend fun receive(message: Message) {
        super.receive(message)

        when (message) {
            is JoinInMessage -> joinInMessage(message, message.player)
            is PlayerMoveMessage -> playerMoveMessage(message.player, message.x, message.y)
            is ExitGameMessage -> exitGameMessage(message)
            is DisconnectedMessage -> disconnectedMessage(message)
            is PlayAgainResponseLocalMessage -> if (message.isAccepted) playAgainResponseLocalMessage(message)
            is PlayAgainResponseRemoteMessage -> if (message.isAccepted) playAgainResponseRemoteMessage(message)
        }
    }

    private fun joinInMessage(message: JoinInMessage, player: Player) {
        val game = this.game ?: Game()
        this.game = game

        if (game.addPlayer(player) is Result.Success) {
            this send message to controller
            startTheGame()
        }
    }

    private fun playerMoveMessage(player: Player?, x: Int, y: Int) {
        if (player != null) {
            when (val result = game?.makeMove(player, x, y)) {
                is Result.Success -> sendGameStatus(result.state)
                is Result.Failure -> logger.e(tag, "make move error: ${result.msg}")
            }
        }
    }

    private fun startTheGame() {
        when (val result = game?.start()) {
            is Result.Success -> {
                sendGameStatus(result.state)
            }
        }
    }

    private fun sendGameStatus(state: GameState) {
        this send GameStateUpdateLocalMessage(gameState = state) to modelManager
    }

    private fun exitGameMessage(message: ExitGameMessage) {
        this.game = null
        this send message to controller
    }

    private fun disconnectedMessage(message: DisconnectedMessage) {
        this.game = null
    }

    private fun playAgainResponseRemoteMessage(message: PlayAgainResponseRemoteMessage) {
        when (val result = game?.restart()) {
            is Result.Success -> {
                sendGameStatus(result.state)
            }
        }
    }

    private fun playAgainResponseLocalMessage(message: PlayAgainResponseLocalMessage) {
        when (val result = game?.restart()) {
            is Result.Success -> {
                sendGameStatus(result.state)
            }
        }
    }
}
