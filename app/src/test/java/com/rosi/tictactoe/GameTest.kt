package com.rosi.tictactoe

import com.rosi.tictactoe.model.game.*
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf

class GameTest : FunSpec({
    
    test("game end without a winner") {

        val game = Game()

        val player1 = (game.addPlayer(Player("1")) as Result.Success).state
        val player2 = (game.addPlayer(Player("2")) as Result.Success).state
        val result = game.start()
        assert(result is Result.Success)

        game.makeMove(player1, 0, 0)
        game.makeMove(player2, 1, 0)
        game.makeMove(player1, 2, 0)

        game.makeMove(player2, 0, 1)
        game.makeMove(player1, 2, 1)
        game.makeMove(player2, 1, 1)

        game.makeMove(player1, 0, 2)
        game.makeMove(player2, 2, 2)
        val makeMoveResult = game.makeMove(player1, 1, 2)

        makeMoveResult.shouldBeInstanceOf<Result.Success<GameState>>() {
            it.state.status shouldBe GameStatus.End
            it.state.winner shouldBe Player.none
        }
    }
})
