package com.rosi.tictactoe.base.logging

interface Logger {
    fun e(tag: String, msg: String, e: Throwable? = null)
    fun e(tag: String, msg: String) = e(tag, msg, null)

    fun w(tag: String, msg: String, e: Throwable? = null)
    fun w(tag: String, msg: String) = w(tag, msg, null)

    fun i(tag: String, msg: String, e: Throwable? = null)
    fun i(tag: String, msg: String) = i(tag, msg, null)

    fun d(tag: String, msg: String, e: Throwable? = null)
    fun d(tag: String, msg: String) = d(tag, msg, null)

    fun testPrint(tag: String, message: Any)
}


