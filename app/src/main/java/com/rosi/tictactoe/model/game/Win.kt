package com.rosi.tictactoe.model.game

sealed class Win {
    data class My(val player: Player) : Win()
    data class Other(val player: Player) : Win()
    object Draw : Win()
}