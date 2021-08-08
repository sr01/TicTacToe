package com.rosi.tictactoe.model.game

data class Player(val name: String, val token: PlayerToken = PlayerToken.None) {
    companion object {
        val none = Player("none", PlayerToken.None)
    }
}

enum class PlayerToken {
   None, X, O
}