package com.rosi.tictactoe.view.com

/**
 *          A                 B
 *         ---               ---
 *
 *   ->  connectTo   -->  onIncomingCall ->
 *   <-  onConnected <--  acceptCall     <-
 *                  ~ ~ ~
 *   <-  onDisconnected <-- rejectCall   <-
 *                  ~ ~ ~
 *   <-  onFailedToConnect
 *                  ~ ~ ~
 *   ->  disconnectFrom ---> onDisconnected ->
 *   <-  onDisconnected
 *                  ~ ~ ~
 *       sendTo      --->    onMessageReceived
 */
interface GameCommService {

    fun start()

    fun stop()

    fun connect(address: String)

    fun disconnect()

    fun acceptCall(address: String)

    fun rejectCall(address: String)

    fun addListener(listener: Listener)

    fun removeListener(listener: Listener)

    interface Listener {
        fun onIncomingCall(address: String)

        fun onConnected(address: String)

        fun onFailedToConnect(address: String)

        fun onDisconnected(address: String, reason: String)
    }
}