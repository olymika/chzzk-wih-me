package org.olymika.chzzkwithme.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.olymika.chzzkwithme.chat.ChzzkSendChatBase
import kotlin.time.Duration

object ChzzkChatUtils {
    fun createSendChatMessage(
        message: String,
        chatChannelId: String,
        sid: String,
        extraToken: String,
    ) = toJson(
        ChzzkSendChatBase(
            cid = chatChannelId,
            sid = sid,
            bdy =
                ChzzkSendChatBase.Body(
                    extras = toJson(ChzzkSendChatBase.Extras(extraToken = extraToken, streamChannelId = chatChannelId)),
                    msg = message,
                ),
        ),
    )
}

internal fun CoroutineScope.launchPeriod(
    interval: Duration,
    action: suspend () -> Unit,
) {
    launch {
        while (true) {
            action()
            delay(interval)
        }
    }
}
