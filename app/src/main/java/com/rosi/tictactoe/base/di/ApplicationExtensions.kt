package com.rosi.tictactoe.base.di

import android.app.Application
import android.content.Context
import com.rosi.tictactoe.TicTacToeApplication

val Context.dependencyProvider: DependencyProvider
    get() = (this.applicationContext as TicTacToeApplication).dependencyProvider

val Application.dependencyProvider: DependencyProvider
    get() = (this as TicTacToeApplication).dependencyProvider
