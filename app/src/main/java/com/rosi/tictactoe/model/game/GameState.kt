package com.rosi.tictactoe.model.game

data class GameState(
    val status: GameStatus,
    val board: Board,
    val player1: Player,
    val player2: Player,
    val currentPlayer: Player,
    val winner: Player,
    val myPlayer: Player,
    val otherPlayer: Player,
    val win: Win?) {

    val allPlayersSet: Boolean
        get() = player1 != Player.none && player2 != Player.none

}