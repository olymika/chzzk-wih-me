package org.olymika.chzzkwithme

import org.olymika.chzzkwithme.AccessTokenContent.ChzzkUserToken

interface ChzzkHttpClient : ChzzkHttpClientProvider, UserApiSupporter

interface ChzzkHttpClientProvider {
    suspend fun getChatChannelId(channelId: String): String

    suspend fun getToken(
        chatChannelId: String,
        auth: ChzzkUserAuth? = null,
    ): ChzzkUserToken

    suspend fun close()
}

interface UserApiSupporter {
    suspend fun getUser(auth: ChzzkUserAuth? = null): ChzzkUser
}
