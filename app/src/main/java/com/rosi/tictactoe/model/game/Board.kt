package com.rosi.tictactoe.model.game

typealias Board = List<List<Cell>>

fun createBoard(): Board = listOf(
    listOf(Cell(), Cell(), Cell()),
    listOf(Cell(), Cell(), Cell()),
    listOf(Cell(), Cell(), Cell())
)

fun Board.hasEmptyCell(): Boolean = flatten().any { it == Cell.empty }

fun Board.noEmptyCell(): Boolean = !hasEmptyCell()

fun Board.forEachCell(cellAction: (y: Int, x: Int, cell: Cell) -> Unit) {
    forEachIndexed { y, list ->
        list.forEachIndexed { x, cell ->
            cellAction(y, x, this[y][x])
        }
    }
}

fun Board.copy(): Board = this.map { it.map { cell -> cell.copy() }.toList() }.toList()