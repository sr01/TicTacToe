package com.rosi.tictactoe.view.com

import com.google.gson.Gson
import com.sr01.p2p.peer.MessageProtocol
import java.io.DataInput
import java.io.DataOutput

object GameMessageJsonProtocol : MessageProtocol<GameComMessage> {
    override fun read(input: DataInput): GameComMessage {
        return when (GameComMessage.MessageType.values()[input.readInt()]) {
            GameComMessage.MessageType.ConnectionAction -> serializer.fromJson(input.readUTF(), GameComMessage.ConnectionActionMessage::class.java)
            GameComMessage.MessageType.Identity -> serializer.fromJson(input.readUTF(), GameComMessage.IdentityMessage::class.java)
            GameComMessage.MessageType.PlayerMove -> serializer.fromJson(input.readUTF(), GameComMessage.PlayerMoveMessage::class.java)
            GameComMessage.MessageType.GameStatus -> serializer.fromJson(input.readUTF(), GameComMessage.GameStateUpdateMessage::class.java)
            GameComMessage.MessageType.PlayAgainRequest -> serializer.fromJson(input.readUTF(), GameComMessage.PlayAgainRequestMessage::class.java)
            GameComMessage.MessageType.PlayAgainResponse -> serializer.fromJson(input.readUTF(), GameComMessage.PlayAgainResponseMessage::class.java)
        }
    }

    override fun write(output: DataOutput, message: GameComMessage) {
        val json = serializer.toJson(message)
        output.writeInt(message.messageType.ordinal)
        output.writeUTF(json)
    }

    private val serializer = Gson()
}


