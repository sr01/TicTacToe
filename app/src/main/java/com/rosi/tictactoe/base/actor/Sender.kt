package com.rosi.tictactoe.base.actor

import com.rosi.tictactoe.base.logging.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.actor
import java.lang.Exception

const val ACTOR_CAPACITY = 10

interface ISender {
    val name: String
    val actor: SendChannel<Message>
    val logger: Logger

    suspend fun receive(message: Message)
}

object DefaultSender : ISender {
    override val name: String = "DefaultSender"
    override val logger: Logger = object : Logger {
        override fun e(tag: String, msg: String, e: Throwable?) {
        }

        override fun w(tag: String, msg: String, e: Throwable?) {
        }

        override fun i(tag: String, msg: String, e: Throwable?) {
        }

        override fun d(tag: String, msg: String, e: Throwable?) {
        }

        override fun testPrint(tag: String, message: Any) {
        }
    }
    override val actor: SendChannel<Message> = createActor(GlobalScope)
    override suspend fun receive(message: Message) {
    }
}

infix fun ISender.send(message: Message): SendTo {
    return SendTo(this, message)
}

infix fun ISender.accept(message: Message) {
    val result = actor.offer(message)

    if (!result) {
        logger.testPrint(name, "failed to accept message: $message")
    }
}

fun ISender.printUnknownMessage(message: Message) {
    logger.e(name, "RECEIVE UNKNOWN MESSAGE: $message")
}

@ObsoleteCoroutinesApi
fun ISender.createActor(scope: CoroutineScope): SendChannel<Message> {
    return scope.actor(
        capacity = ACTOR_CAPACITY,
        block = {
            for (message in channel) {
                logger.testPrint(name, message)
                try {
                    receive(message)
                } catch (e: Exception) {
                    logger.e("ISender", "actor $name failed to receive message: $message", e)
                }
            }
        })
}

class SendTo(val from: ISender, val message: Message) {
    infix fun to(to: ISender?) {
        to?.accept(message.withSender(from))
    }
}

interface Message {
    val sender: ISender
    fun withSender(sender: ISender): Message
}