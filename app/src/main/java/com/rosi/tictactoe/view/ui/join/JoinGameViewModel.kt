package com.rosi.tictactoe.view.ui.join

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.rosi.tictactoe.base.logging.Logger
import com.rosi.tictactoe.view.GameUIEvent
import com.rosi.tictactoe.view.MainActivityActor
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class JoinGameViewModel(private val actor: MainActivityActor, private val logger: Logger) : ViewModel() {

    private val tag = "JoinGameViewModel"
    private val internalNotifications = Channel<GameUIEvent>()
    val playerName: LiveData<String>
    val notifications: Flow<GameUIEvent>

    init {
        logger.d(tag, "init")

        playerName = actor.getEventFlow()
            .filter { it is GameUIEvent.PlayerNameUIEvent }
            .map { (it as GameUIEvent.PlayerNameUIEvent).playerName }
            .asLiveData()

        val externalNotifications2: Flow<GameUIEvent> = actor.getEventFlow()

        notifications = flowOf(externalNotifications2, internalNotifications.receiveAsFlow()).flattenMerge()
    }

    fun joinInTheGame(playerName: String) {
        viewModelScope.launch {
            if (isValidPlayerName(playerName)) {
                actor.setPlayerName(playerName)
                internalNotifications.send(GameUIEvent.NavigateToPlayersUIEvent)
            } else {
                internalNotifications.send(GameUIEvent.InvalidPlayerNameUIEvent)
            }
        }
    }

    private fun isValidPlayerName(playerName: String): Boolean {
        return playerName.isNotBlank()
    }
}

