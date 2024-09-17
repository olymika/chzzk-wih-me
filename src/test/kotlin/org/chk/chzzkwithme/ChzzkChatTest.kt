package org.chk.chzzkwithme

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.chk.chzzkwithme.TestChannel.HAN_DONG_SUK
import org.chk.chzzkwithme.chat.ChzzkChatListener
import org.chk.chzzkwithme.chat.ChzzkChatMessage
import org.chk.chzzkwithme.chat.chatHandler
import kotlin.test.Test
import kotlin.test.assertTrue

class ChzzkChatTest {
    @Test
    fun testChatListenMessage() {
        val chzzk =
            chzzk {
                channelId = HAN_DONG_SUK

                client {
                    loggingLevel = LoggingLevel.ALL
                }
            }

        val listener =
            object : ChzzkChatListener {
                val messages = mutableListOf<String>()

                override suspend fun receive(message: ChzzkChatMessage) {
                    messages += message.chats.map { it.msg }
                }
            }

        val chatHandler = chatHandler { addListener(listener) }

        runBlocking {
            val job = launch(Dispatchers.IO) { chzzk.chat(chatHandler) }

            delay(5000)

            job.cancel()
        }

        assertTrue(listener.messages.isNotEmpty())
    }
}
