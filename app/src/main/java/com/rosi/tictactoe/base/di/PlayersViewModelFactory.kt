package com.rosi.tictactoe.base.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rosi.tictactoe.view.ui.main.MainViewModel
import com.rosi.tictactoe.view.ui.players.PlayersViewModel
import java.lang.IllegalArgumentException

class PlayersViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(PlayersViewModel::class.java)) {
            with(context.dependencyProvider) {
                PlayersViewModel(controller.viewManager.mainActivityActor, logger) as T
            }
        } else {
            throw IllegalArgumentException("ViewModel not found")
        }
    }
}