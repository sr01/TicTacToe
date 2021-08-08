package com.rosi.tictactoe.model.game

data class Cell(var player: Player = Player.none) {
    companion object {
        val empty = Cell(Player.none)
    }
}