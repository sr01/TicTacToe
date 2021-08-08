package com.rosi.tictactoe.model.settings

interface Settings {

    fun getComPort() : Int

    fun getPlayerName(): String

    fun setPlayerName(playerName: String)
}