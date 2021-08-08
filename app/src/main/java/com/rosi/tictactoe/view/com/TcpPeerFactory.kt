package com.rosi.tictactoe.view.com

import com.sr01.p2p.peer.tcp.TcpPeer

fun interface TcpPeerFactory<TMessage> {
    fun create(): TcpPeer<TMessage>
}