package com.rosi.tictactoe

import com.rosi.tictactoe.base.actor.Actor
import com.rosi.tictactoe.base.actor.Message
import com.rosi.tictactoe.utils.ConsoleLogger
import kotlinx.coroutines.test.TestCoroutineScope
import java.util.concurrent.LinkedBlockingQueue

class TestActor(name: String = "tests/test-actor") : Actor(name, ConsoleLogger, TestCoroutineScope()) {
    private val messages = LinkedBlockingQueue<Message>()

    override suspend fun receive(message: Message) {
        super.receive(message)

        messages.put(message)
    }

    val firstMessage: Message?
        get() {
            return messages.poll()
        }

    fun clearMessages() {
        messages.clear()
    }
}