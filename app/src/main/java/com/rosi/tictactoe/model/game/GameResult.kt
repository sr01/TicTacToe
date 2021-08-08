package com.rosi.tictactoe.model.game

//sealed class GameResult {
//    data class Success(val state: GameState) : GameResult()
//    data class Failure(val msg: String) : GameResult()
//}

sealed class Result<T> {
    data class Success<T>(val state: T) : Result<T>()
    data class Failure<T>(val msg: String) : Result<T>()
}