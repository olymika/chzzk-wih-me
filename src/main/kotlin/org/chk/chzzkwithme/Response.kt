package org.chk.chzzkwithme

import kotlinx.serialization.Serializable

@Serializable
data class LiveDetailContent(
    val content: ChatChannelId,
) {
    @Serializable
    data class ChatChannelId(
        val chatChannelId: String,
    )
}

@Serializable
data class AccessTokenContent(
    val content: ChzzkUserToken,
) {
    @Serializable
    data class ChzzkUserToken(
        val accessToken: String,
        val extraToken: String,
    )
}

@Serializable
data class ChzzkUserContent(
    val content: ChzzkUser,
)
