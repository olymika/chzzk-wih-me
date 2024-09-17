package org.chk.chzzkwithme.chat

import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.close
import java.util.concurrent.atomic.AtomicReference

class ChzzkChatHandler internal constructor(
    internal val listeners: List<ChzzkChatListener> = mutableListOf(),
) {
    internal val sender: ChzzkChatSender = ChzzkChatSender()
    internal val session: AtomicReference<WebSocketSession> = AtomicReference()

    fun send(message: String) {
        sender.send(message)
    }

    suspend fun close() {
        session.get().close()
    }
}

fun chatHandler(block: ChzzkChatHandlerConfig.() -> Unit): ChzzkChatHandler = ChzzkChatHandler(ChzzkChatHandlerConfig().apply(block).listeners)

class ChzzkChatHandlerConfig {
    internal val listeners: MutableList<ChzzkChatListener> = mutableListOf()

    fun addListener(listener: ChzzkChatListener) {
        listeners.add(listener)
    }
}
