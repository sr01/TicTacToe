package com.rosi.tictactoe.utils

import com.rosi.tictactoe.base.logging.Logger

object P2PLogger : com.sr01.p2p.utils.Logger {

    private val logger: Logger = AndroidLogger

    override fun e(tag: String, msg: String, e: Throwable?) {
        if (e != null) logger.e(tag, msg, e) else logger.e(tag, msg)
    }

    override fun w(tag: String, msg: String, e: Throwable?) {
        if (e != null) logger.w(tag, msg, e) else logger.w(tag, msg)
    }

    override fun i(tag: String, msg: String, e: Throwable?) {
        if (e != null) logger.i(tag, msg, e) else logger.i(tag, msg)
    }

    override fun d(tag: String, msg: String, e: Throwable?) {
        if (e != null) logger.d(tag, "$msg, Exception: $e") else logger.d(tag, msg)
    }

    override fun v(tag: String, msg: String, e: Throwable?) {
        if (e != null) logger.d(tag, msg, e) else logger.d(tag, msg)
    }
}