package com.rosi.tictactoe.base.di

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.rosi.tictactoe.view.ui.board.BoardViewModel
import com.rosi.tictactoe.view.ui.main.MainViewModel
import java.lang.IllegalArgumentException

class BoardViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return if (modelClass.isAssignableFrom(BoardViewModel::class.java)) {
            with(context.dependencyProvider) {
                BoardViewModel(controller.viewManager.mainActivityActor, logger) as T
            }
        } else {
            throw IllegalArgumentException("ViewModel not found")
        }
    }
}