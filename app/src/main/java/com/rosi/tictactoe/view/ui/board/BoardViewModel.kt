package com.rosi.tictactoe.view.ui.board

import androidx.lifecycle.ViewModel
import com.rosi.tictactoe.base.logging.Logger
import com.rosi.tictactoe.view.GameUIEvent
import com.rosi.tictactoe.view.MainActivityActor
import kotlinx.coroutines.flow.Flow

class BoardViewModel(private val actor: MainActivityActor, private val logger: Logger) : ViewModel() {

    private val tag = "BoardViewModel"

    val notifications: Flow<GameUIEvent>

    init {
        logger.d(tag, "init")

        notifications = actor.getEventFlow()
    }

    fun makeGameMove(x: Int, y: Int) {
        actor.makeGameMove(x, y)
    }

    fun exitGame() {
        actor.exitGame()
    }

    fun playAgain() {
        actor.playAgain()
    }

    fun acceptPlayAgain() {
        actor.acceptPlayAgain()
    }

    fun declinePlayAgain() {
        actor.declinePlayAgain()
    }
}
