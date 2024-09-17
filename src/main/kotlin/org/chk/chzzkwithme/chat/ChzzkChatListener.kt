package org.chk.chzzkwithme.chat

interface ChzzkChatListener {
    suspend fun receive(message: ChzzkChatMessage)
}
