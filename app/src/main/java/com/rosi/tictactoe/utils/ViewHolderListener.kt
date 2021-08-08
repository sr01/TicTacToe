package com.rosi.tictactoe.utils

interface ViewHolderListener<T> {
    fun onViewHolderClick(item : T)
    fun onViewHolderLongClick(item : T) : Boolean
}