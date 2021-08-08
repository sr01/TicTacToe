package com.rosi.tictactoe.view.settings

import android.content.SharedPreferences
import android.content.res.Resources
import com.rosi.tictactoe.model.settings.Settings

class SharedPreferencesSettings(private val resources: Resources, private val prefs: SharedPreferences) : Settings {

    override fun setPlayerName(playerName: String) {
        prefs.edit()
            .putString(PLAYER_NAME_KEY, playerName)
            .apply()
    }

    override fun getComPort(): Int {
        return prefs.getInt(COM_PORT, 10701)
    }

    override fun getPlayerName(): String {
        return prefs.getString(PLAYER_NAME_KEY, getRandomPlayerName())!!
    }

    private fun getRandomPlayerName(): String {
            return "Player-${System.currentTimeMillis()}"
    }

    companion object {
        const val PLAYER_NAME_KEY = "player-name"
        const val COM_PORT = "com-port"
    }

}