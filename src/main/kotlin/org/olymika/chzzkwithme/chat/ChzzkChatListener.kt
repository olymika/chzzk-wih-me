package org.olymika.chzzkwithme.chat

interface ChzzkChatListener {
    suspend fun receive(message: ChzzkChatMessage)
}
