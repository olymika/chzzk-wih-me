package org.olymika.chzzkwithme.chat

import java.util.Queue
import java.util.concurrent.LinkedBlockingQueue

internal class ChzzkChatSender {
    private val messageBuffer: Queue<String> = LinkedBlockingQueue()

    fun send(message: String) {
        messageBuffer.add(message)
    }

    fun extractBuffer(): Queue<String> = messageBuffer
}
