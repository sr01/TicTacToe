package com.rosi.tictactoe

import com.rosi.tictactoe.base.logging.Logger
import com.rosi.tictactoe.base.di.DependencyProvider
import com.rosi.tictactoe.controller.Controller
import com.rosi.tictactoe.model.settings.Settings
import com.rosi.tictactoe.utils.ConsoleLogger
import com.rosi.tictactoe.view.com.GameComMessage
import com.rosi.tictactoe.view.com.TcpPeerFactory
import com.sr01.p2p.discovery.NetworkDiscoveryService
import com.sr01.p2p.identity.IdentityProvider
import com.sr01.p2p.identity.NameIdentity
import com.sr01.p2p.peer.tcp.TcpPeer
import io.kotest.core.spec.style.FunSpec
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.test.TestCoroutineScope
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify

class ApplicationFlowsTest : FunSpec({

    val testScope = TestCoroutineScope()
    val testActor = TestActor()
    val tcpPeer: TcpPeer<GameComMessage> = mock()

    val dependencyProvider = object : DependencyProvider {
        override val logger: Logger = ConsoleLogger
        override val settings: Settings = mock()
        override val nds: NetworkDiscoveryService<NameIdentity> = mock()
        override val identityProvider: IdentityProvider<NameIdentity> = mock()
        override val mainScope: CoroutineScope = testScope
        override val ioScope: CoroutineScope = testScope
        override val generalScope: CoroutineScope = testScope
        override val controller: Controller by lazy {
            Controller(this)
        }
        override val peerFactory: TcpPeerFactory<GameComMessage> by lazy {
            TcpPeerFactory {
                tcpPeer
            }
        }

    }
    val controller = dependencyProvider.controller
    val mainActivityActor = controller.viewManager.mainActivityActor

    test("AppStartMessage") {
        mainActivityActor.appStart()

        verify(tcpPeer, times(1)).start()
    }

    test("AppStopMessage") {

        mainActivityActor.appStop()

        verify(tcpPeer, times(1)).stop()
    }

})