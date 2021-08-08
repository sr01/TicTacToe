package com.rosi.tictactoe.base.actor

import com.rosi.tictactoe.base.logging.Logger
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.SendChannel

abstract class Actor(override val name: String, override val logger: Logger, private val scope: CoroutineScope) : ISender {
    override val actor: SendChannel<Message> = createActor(scope)
    private val scheduledMessages = mutableMapOf<Message, Job>()

    override fun toString(): String = name

    override suspend fun receive(message: Message) {
        when (message) {
            is ScheduledMessage -> onScheduledMessage(message.scheduledMessage, message.recipient, message.delayMillis)
            is CancelScheduledMessage -> onCancelScheduledMessage(message.scheduledMessage)
        }
    }

    fun scheduleMessage(message: Message, recipient: ISender, delayMillis: Long) {
        val self = this@Actor

        scope.launch {
            self send ScheduledMessage(scheduledMessage = message, recipient = recipient, delayMillis = delayMillis) to self
        }
    }

    fun cancelScheduledMessage(message: Message) {
        val self = this@Actor

        scope.launch {
            self send CancelScheduledMessage(scheduledMessage = message) to self
        }
    }

    private suspend fun onScheduledMessage(scheduledMessage: Message, recipient: ISender, delayMillis: Long) {
        scheduledMessages.remove(scheduledMessage)?.let { job ->
            job.cancel()
        }

        val job = scope.launch {
            delay(delayMillis)
            this@Actor send scheduledMessage to recipient
        }

        scheduledMessages[scheduledMessage] = job
    }

    private fun onCancelScheduledMessage(scheduledMessage: Message) {
        scheduledMessages.remove(scheduledMessage)?.let { job ->
            job.cancel()
        }
    }
}

data class ScheduledMessage(override val sender: ISender = DefaultSender, val scheduledMessage: Message, val delayMillis: Long, val recipient: ISender) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
}

data class CancelScheduledMessage(override val sender: ISender = DefaultSender, val scheduledMessage: Message) : Message {
    override fun withSender(sender: ISender) = this.copy(sender = sender)
}
