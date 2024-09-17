package org.chk.chzzkwithme.chat

import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.send
import kotlinx.coroutines.coroutineScope
import org.chk.chzzkwithme.AccessTokenContent
import org.chk.chzzkwithme.HttpClientConfig
import org.chk.chzzkwithme.chat.ChzzkMesssageType.Commands.CHAT
import org.chk.chzzkwithme.chat.ChzzkMesssageType.Commands.CONNECTED
import org.chk.chzzkwithme.chat.ChzzkMesssageType.Commands.DONATION
import org.chk.chzzkwithme.chat.ChzzkMesssageType.Commands.PING
import org.chk.chzzkwithme.chat.ChzzkMesssageType.Commands.RECENT_CHAT
import org.chk.chzzkwithme.utils.fromJson
import org.chk.chzzkwithme.utils.readTree
import org.chk.chzzkwithme.utils.toJson
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

interface ChzzkWsClient {
    val connectSendFlag: AtomicBoolean
    val sid: AtomicReference<String>

    val handler: ChzzkChatReceiveHandler

    suspend fun connect(
        chatHandler: ChzzkChatHandler,
        chatChannelId: String,
        userToken: AccessTokenContent.ChzzkUserToken,
        userHashId: String?,
    )
}

interface ChzzkWsClientFactory {
    fun create(
        config: HttpClientConfig,
        handler: ChzzkChatHandler,
    ): ChzzkWsClient
}

class ChzzkChatReceiveHandler(
    private val listeners: List<ChzzkChatListener>,
) {
    suspend fun handle(
        message: String,
        ws: WebSocketSession,
        sidRef: AtomicReference<String>,
    ) = coroutineScope {
        val cmdNode = readTree(message).get("cmd")

        if (cmdNode.isMissingNode) {
            return@coroutineScope
        }

        val cmd = cmdNode.asInt()

        when (cmd) {
            CONNECTED -> {
                val connectedMessage = fromJson<ChzzkChatConnectBase>(message)
                sidRef.set(connectedMessage.bdy.sid)
            }

            PING -> {
                ws.send(toJson(ChzzkChatBoundPong()))
            }

            RECENT_CHAT -> {
                val recentChatMessage = fromJson<ChzzkRecentChatBase>(message)
                recentChatMessage.bdy.messageList.forEach {
                    if (it.userId != "@OPEN") {
                        println(it)
                    }
                }
            }

            CHAT ->
                listeners.forEach {
                    it.receive(
                        fromJson<ChzzkChatReceiveMessage>(message).toChatMessage(
                            ChzzkChatType.NORMAL,
                        ),
                    )
                }
            DONATION ->
                listeners.forEach {
                    it.receive(
                        fromJson<ChzzkChatReceiveMessage>(message).toChatMessage(
                            ChzzkChatType.DONATION,
                        ),
                    )
                }

            else -> {}
        }
    }
}
