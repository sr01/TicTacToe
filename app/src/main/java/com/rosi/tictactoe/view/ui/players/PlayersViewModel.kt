package com.rosi.tictactoe.view.ui.players

import androidx.lifecycle.ViewModel
import com.rosi.tictactoe.base.logging.Logger
import com.rosi.tictactoe.model.connect.User
import com.rosi.tictactoe.view.GameUIEvent
import com.rosi.tictactoe.view.MainActivityActor
import kotlinx.coroutines.flow.Flow

class PlayersViewModel(private val actor: MainActivityActor, private val logger: Logger) : ViewModel() {

    private val tag = "PlayersViewModel"

    val notifications: Flow<GameUIEvent>

    init {
        logger.d(tag, "init")

        notifications = actor.getEventFlow()
    }

    fun onViewCreated() {
        actor.startDiscover()
    }

    fun onViewDestroyed() {
        actor.stopDiscover()
    }

    fun connectToUser(user: User) {
        actor.connectToUser(user)
    }
}