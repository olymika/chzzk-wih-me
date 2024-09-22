package org.olymika.chzzkwithme

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.olymika.chzzkwithme.AccessTokenContent.ChzzkUserToken
import org.olymika.chzzkwithme.chat.ChzzkChatHandler
import org.olymika.chzzkwithme.chat.ChzzkWsClientFactory
import org.olymika.chzzkwithme.chat.Ktor

class Chzzk internal constructor(
    private val channelId: String?,
    private val chzzkUserAuth: ChzzkUserAuth?,
    private val client: ChzzkHttpClient,
    private val clientConfig: HttpClientConfig,
) {
    suspend fun getChatChannelId(channelId: String? = null): String = client.getChatChannelId(channelId ?: this.channelId ?: throw IllegalStateException("not default chzzk channelId"))

    suspend fun getToken(chatChannelId: String): ChzzkUserToken = client.getToken(chatChannelId, this.chzzkUserAuth)

    suspend fun getUser(auth: ChzzkUserAuth? = null): ChzzkUser = client.getUser(auth ?: this.chzzkUserAuth)

    suspend fun getFollowingChannels(
        page: Int = 0,
        size: Long = 505,
        auth: ChzzkUserAuth? = null,
    ): List<FollowingChannel> = client.getFollowingChannels(page, size, auth ?: this.chzzkUserAuth)

    suspend fun chat(
        handler: ChzzkChatHandler,
        factory: ChzzkWsClientFactory = Ktor,
    ) = coroutineScope {
        val chatChannelId = async { getChatChannelId() }
        val chzzkUser = async { getUser() }
        val userToken = async { getToken(chatChannelId.await()) }

        val wsClient = factory.create(clientConfig, handler)

        launch(Dispatchers.IO) {
            wsClient.connect(
                chatHandler = handler,
                chatChannelId = chatChannelId.await(),
                userToken = userToken.await(),
                userHashId = chzzkUser.await().userIdHash,
            )
        }
    }

    suspend fun close() {
        client.close()
    }
}

class ChzzkUserAuth(
    authConfig: AuthConfig,
) {
    val aut: String = authConfig.nidAut
    val ses: String = authConfig.nidSes
}

fun <T> blocking(block: suspend CoroutineScope.() -> T): T =
    runBlocking(Dispatchers.IO) {
        block()
    }
