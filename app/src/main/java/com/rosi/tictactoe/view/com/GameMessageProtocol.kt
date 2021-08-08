package com.rosi.tictactoe.view.com

import com.rosi.tictactoe.model.game.*
import com.sr01.p2p.identity.NameIdentity
import com.sr01.p2p.peer.MessageProtocol
import java.io.DataInput
import java.io.DataOutput

object GameMessageProtocol : MessageProtocol<GameComMessage> {
    override fun read(input: DataInput): GameComMessage {
        return when (GameComMessage.MessageType.values()[input.readInt()]) {
            GameComMessage.MessageType.ConnectionAction -> GameComMessage.ConnectionActionMessage(GameComMessage.ConnectionActions.values()[input.readInt()])
            GameComMessage.MessageType.Identity -> GameComMessage.IdentityMessage(NameIdentity(input.readUTF(), input.readUTF(), input.readUTF(), input.readInt()))
            GameComMessage.MessageType.PlayerMove -> GameComMessage.PlayerMoveMessage(
                input.readPlayer(),
                input.readInt(),
                input.readInt()
            )
            GameComMessage.MessageType.GameStatus -> GameComMessage.GameStateUpdateMessage(
                GameStatus.values()[input.readInt()],
                input.readBoard(),
                input.readPlayer(),
                input.readPlayer(),
                input.readPlayer(),
                input.readPlayer()
            )
            GameComMessage.MessageType.PlayAgainRequest -> GameComMessage.PlayAgainRequestMessage(input.readPlayer())
            GameComMessage.MessageType.PlayAgainResponse -> GameComMessage.PlayAgainResponseMessage(input.readPlayer(), input.readBoolean())
        }
    }

    override fun write(output: DataOutput, message: GameComMessage) {
        when (message) {
            is GameComMessage.ConnectionActionMessage -> {
                output.writeInt(GameComMessage.MessageType.ConnectionAction.ordinal)
                output.writeInt(message.action.ordinal)
            }
            is GameComMessage.IdentityMessage -> {
                output.writeInt(GameComMessage.MessageType.Identity.ordinal)
                output.writeUTF(message.identity.name)
                output.writeUTF(message.identity.description)
                output.writeUTF(message.identity.host)
                output.writeInt(message.identity.port)
            }
            is GameComMessage.PlayerMoveMessage -> {
                output.writeInt(GameComMessage.MessageType.PlayerMove.ordinal)
                output.writePlayer(message.player)
                output.writeInt(message.x)
                output.writeInt(message.y)
            }
            is GameComMessage.GameStateUpdateMessage -> {
                output.writeInt(GameComMessage.MessageType.GameStatus.ordinal)
                output.writeInt(message.status.ordinal)
                output.writeBoard(message.board)
                output.writePlayer(message.currentPlayer)
                output.writePlayer(message.player1)
                output.writePlayer(message.player2)
                output.writePlayer(message.winner)
            }
            is GameComMessage.PlayAgainRequestMessage -> {
                output.writeInt(GameComMessage.MessageType.PlayAgainRequest.ordinal)
                output.writePlayer(message.requestBy)
            }
            is GameComMessage.PlayAgainResponseMessage -> {
                output.writeInt(GameComMessage.MessageType.PlayAgainResponse.ordinal)
                output.writePlayer(message.answeredBy)
                output.writeBoolean(message.isAccepted)
            }
        }
    }
}

private fun DataInput.readBoard(): List<List<Cell>> = createBoard().apply {
    forEachCell { _, _, cell -> cell.player = Player(readUTF(), PlayerToken.values()[readInt()]) }
}

private fun DataOutput.writeBoard(board: List<List<Cell>>) {
    board.forEachCell { _, _, cell ->
        writeUTF(cell.player.name)
        writeInt(cell.player.token.ordinal)
    }
}

private fun DataOutput.writePlayer(player: Player) {
    writeUTF(player.name)
    writeInt(player.token.ordinal)
}

private fun DataInput.readPlayer(): Player =
    Player(readUTF(), PlayerToken.values()[readInt()])

