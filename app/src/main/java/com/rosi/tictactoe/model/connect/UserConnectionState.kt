package com.rosi.tictactoe.model.connect

enum class UserConnectionState {
    Disconnected,
    Connecting,
    AwaitForApproval,
    Connected,
    FailedToConnect
}