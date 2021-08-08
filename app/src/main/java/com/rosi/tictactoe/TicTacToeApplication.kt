package com.rosi.tictactoe

import android.app.Application
import com.rosi.tictactoe.base.di.AndroidDependencyProvider
import com.rosi.tictactoe.base.di.DependencyProvider

class TicTacToeApplication : Application() {

    private val tag = "TicTacToeApplication"

    val dependencyProvider: DependencyProvider by lazy {
        AndroidDependencyProvider(this.applicationContext)
    }

    override fun onCreate() {
        super.onCreate()

        dependencyProvider.logger.d(tag, "onCreate")
    }
}