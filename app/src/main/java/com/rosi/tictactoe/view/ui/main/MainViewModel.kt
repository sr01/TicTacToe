package com.rosi.tictactoe.view.ui.main

import androidx.lifecycle.ViewModel
import com.rosi.tictactoe.base.logging.Logger
import com.rosi.tictactoe.model.connect.User
import com.rosi.tictactoe.view.MainActivityActor

class MainViewModel(private val actor: MainActivityActor, private val logger: Logger) : ViewModel() {

    private val tag = "MainViewModel"

    init {
        logger.d(tag, "init")
    }

    fun appStart() {
        actor.appStart()
    }

    fun appStop() {
        actor.appStop()
    }

    fun onPause() {
        logger.testPrint(tag, "onPause, ")
        actor.appPause()
    }

    fun onResume() {
        logger.testPrint(tag, "onResume, ")
        actor.appResume()
    }

    fun acceptCall(user: User) {
        actor.acceptCall(user)
    }

    fun rejectCall(user: User) {
        actor.rejectCall(user)
    }
}

