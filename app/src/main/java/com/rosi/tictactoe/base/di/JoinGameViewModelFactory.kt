package com.rosi.tictactoe.base.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rosi.tictactoe.view.ui.join.JoinGameViewModel
import com.rosi.tictactoe.view.ui.main.MainViewModel
import java.lang.IllegalArgumentException

class JoinGameViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(JoinGameViewModel::class.java)) {
            with(context.dependencyProvider) {
                JoinGameViewModel(controller.viewManager.mainActivityActor, logger) as T
            }
        } else {
            throw IllegalArgumentException("ViewModel not found")
        }
    }
}