package org.chk.chzzkwithme.chat

import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.ws
import io.ktor.serialization.kotlinx.json.json
import io.ktor.websocket.Frame
import io.ktor.websocket.WebSocketSession
import io.ktor.websocket.readText
import io.ktor.websocket.send
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import org.chk.chzzkwithme.AccessTokenContent
import org.chk.chzzkwithme.HttpClientConfig
import org.chk.chzzkwithme.LoggingLevel
import org.chk.chzzkwithme.utils.ChzzkChatUtils.createSendChatMessage
import org.chk.chzzkwithme.utils.ChzzkUrlUtils
import org.chk.chzzkwithme.utils.toJson
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicReference

object Ktor : ChzzkWsClientFactory {
    override fun create(
        config: HttpClientConfig,
        handler: ChzzkChatHandler,
    ): ChzzkWsClient = KtorChzzkWsClient(config, handler)
}

class KtorChzzkWsClient(
    config: HttpClientConfig,
    handlers: ChzzkChatHandler,
) : ChzzkWsClient {
    override val connectSendFlag = AtomicBoolean(false)
    override val sid: AtomicReference<String> = AtomicReference("")

    override val handler: ChzzkChatReceiveHandler = ChzzkChatReceiveHandler(handlers.listeners)

    private val client: HttpClient =
        HttpClient(OkHttp) {
            engine {
                config {
                    connectTimeout(config.connectionTimeOut, config.connectionTimeUnit)
                    writeTimeout(config.writeTimeOut, config.writeTimeUnit)
                    readTimeout(config.readTimeOut, config.readTimeUnit)
                }
            }

            install(Logging) {
                level =
                    when (config.loggingLevel) {
                        LoggingLevel.NONE -> LogLevel.NONE
                        LoggingLevel.HEADERS -> LogLevel.HEADERS
                        LoggingLevel.BODY -> LogLevel.BODY
                        LoggingLevel.ALL -> LogLevel.ALL
                    }
            }

            install(ContentNegotiation) {
                json(
                    json =
                        Json {
                            ignoreUnknownKeys = true
                        },
                )
            }

            install(WebSockets)
        }

    override suspend fun connect(
        chatHandler: ChzzkChatHandler,
        chatChannelId: String,
        userToken: AccessTokenContent.ChzzkUserToken,
        userHashId: String?,
    ) {
        client.ws(ChzzkUrlUtils.CHAT_URL) {
            chatHandler.session.set(this)

            val sendJob =
                launch(Dispatchers.IO) {
                    while (true) {
                        if (connectSendFlag.get().not()) {
                            sendOpenChatChannel(this@ws, chatChannelId, userToken.accessToken, userHashId)
                        }

                        val sendMessageBuffer = chatHandler.sender.extractBuffer()

                        while (sendMessageBuffer.isNotEmpty()) {
                            val sendMessage = createSendChatMessage(sendMessageBuffer.poll(), chatChannelId, sid.get(), userToken.extraToken)
                            send(sendMessage)
                            delay(1500)
                        }
                    }
                }

            try {
                for (frame in incoming) {
                    when (frame) {
                        is Frame.Text -> handler.handle(frame.readText(), this, sid)
                        is Frame.Close -> {
                            break
                        }
                        else -> {}
                    }
                }
            } finally {
                sendJob.cancelAndJoin()
            }
        }
    }

    private suspend fun sendOpenChatChannel(
        ws: WebSocketSession,
        chatChannelId: String,
        accessToken: String,
        userHashId: String?,
    ) {
        ChzzkChatOpenBase(
            cid = chatChannelId,
            bdy =
                ChzzkChatOpenBase.Body(
                    accTkn = accessToken,
                    uid = userHashId ?: "",
                    auth = if (userHashId == null) "READ" else "SEND",
                ),
        ).let {
            ws.send(toJson(it))
            connectSendFlag.set(true)
        }

        delay(1000)

        ws.send(toJson(ChzzkChatBoundPong()))
    }
}
