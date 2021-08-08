package com.rosi.tictactoe.model.connect

data class User(val name: String, val address: String, val port: Int) {
    var connectionState: UserConnectionState = UserConnectionState.Disconnected
        private set

    fun withConnectionStatus(userConnectionState: UserConnectionState): User =
        this.copy().apply { this.connectionState = userConnectionState }

    override fun toString(): String {
        return "User(name=$name, address=$address, port=$port, connectionState=$connectionState)"
    }
}