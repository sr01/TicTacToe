package com.rosi.tictactoe.utils

import androidx.lifecycle.MutableLiveData
import java.util.concurrent.LinkedBlockingQueue

class NotificationHandler<T> {
    var hasNotifications: MutableLiveData<Event<Boolean>> = MutableLiveData()
    private val notificationsQueue = LinkedBlockingQueue<T>()

    fun addNotification(notification: T) {
        notificationsQueue.add(notification)
        hasNotifications.postValue(Event(true))
    }

    fun getNotifications(): List<T> {
        val list = mutableListOf<T>()
        notificationsQueue.drainTo(list)
        return list
    }
}

open class Event<out T>(private val content: T) {

    var hasBeenHandled = false
        private set // Allow external read but not write

    /**
     * Returns the content and prevents its use again.
     */
    fun getContentIfNotHandled(): T? {
        return if (hasBeenHandled) {
            null
        } else {
            hasBeenHandled = true
            content
        }
    }

    /**
     * Returns the content, even if it's already been handled.
     */
    fun peekContent(): T = content
}