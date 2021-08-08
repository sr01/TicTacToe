package com.rosi.tictactoe.model.game

class Game {

    private var state = GameState(GameStatus.Playing, createBoard(), Player.none, Player.none, Player.none, Player.none, Player.none, Player.none, null)

    private val winCombinations
        get() = arrayOf(
            arrayOf(state.board[0][0], state.board[0][1], state.board[0][2]),
            arrayOf(state.board[1][0], state.board[1][1], state.board[1][2]),
            arrayOf(state.board[2][0], state.board[2][1], state.board[2][2]),
            arrayOf(state.board[0][0], state.board[1][0], state.board[2][0]),
            arrayOf(state.board[0][1], state.board[1][1], state.board[2][1]),
            arrayOf(state.board[0][2], state.board[1][2], state.board[2][2]),
            arrayOf(state.board[0][0], state.board[1][1], state.board[2][2]),
            arrayOf(state.board[0][2], state.board[1][1], state.board[2][0])
        )

    fun addPlayer(player: Player): Result<Player> {
        return when {
            state.player1 == Player.none -> {
                state = state.copy(player1 = player.copy(token = PlayerToken.X))
                Result.Success(state.player1)
            }
            state.player2 == Player.none -> {
                state = state.copy(player2 = player.copy(token = PlayerToken.O))
                Result.Success(state.player2)
            }
            else -> {
                Result.Failure("no more room for another player")
            }
        }
    }

    fun start(): Result<GameState> {
        return if (state.allPlayersSet) {
            state = state.copy(status = GameStatus.Playing, currentPlayer = chooseFirstPlayer(listOf(state.player1, state.player2)))
            Result.Success(state)
        } else {
            Result.Failure("players not set")
        }
    }

    fun makeMove(player: Player, x: Int, y: Int): Result<GameState> = checkMakeMoveConditions(player, x, y) {

        state.board[y][x].player = player

        state = state.copy(winner = findWinner())

        state = if (state.winner != Player.none) {
            state.copy(status = GameStatus.End)
        } else {
            if (state.board.noEmptyCell()) {
                state.copy(status = GameStatus.End)
            } else {
                state.copy(currentPlayer = chooseNextPlayer())
            }
        }

        return@checkMakeMoveConditions Result.Success(state)
    }

    private fun checkMakeMoveConditions(player: Player, x: Int, y: Int, function: () -> Result<GameState>): Result<GameState> {
        return if (state.status == GameStatus.Playing) {
            if (player == state.currentPlayer) {
                val cell = state.board[y][x]
                if (cell == Cell.empty) {
                    function()
                } else {
                    Result.Failure("can't make move: ($x, $y) $player, cell already set: $cell")
                }
            } else {
                Result.Failure("can't make move: ($x, $y) $player, current player: ${state.currentPlayer}")
            }
        } else {
            Result.Failure("can't make move: ($x, $y) $player, current state: ${state.status}")
        }
    }

    private fun findWinner(): Player {
        val winCombination = winCombinations.firstOrNull { isWinCombination(it) != Player.none }
        return when {
            winCombination != null -> winCombination[0].player
            else -> Player.none
        }
    }

    private fun isWinCombination(cells: Array<Cell>): Player {
        return if ((cells[0].player != Player.none) && (cells[0].player == cells[1].player) && (cells[0].player == cells[2].player)) {
            cells[0].player
        } else {
            Player.none
        }
    }

    private fun chooseFirstPlayer(players: Collection<Player>): Player {
        return players.first()
    }

    private fun chooseNextPlayer(): Player {
        return when (state.currentPlayer) {
            state.player1 -> state.player2
            state.player2 -> state.player1
            else -> throw Exception("invalid current player: ${state.currentPlayer}")
        }
    }

    fun restart(): Result.Success<GameState> {
        state = state.copy(
            status = GameStatus.Playing,
            currentPlayer = chooseFirstPlayer(listOf(state.player1, state.player2)),
            board = createBoard(),
            winner = Player.none,
            win = null
        )

        return Result.Success(state)
    }

    data class Point(val x: Int, val y: Int)
}
