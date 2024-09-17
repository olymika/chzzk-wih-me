package org.olymika.chzzkwithme

import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.olymika.chzzkwithme.chat.ChzzkChatListener
import org.olymika.chzzkwithme.chat.ChzzkChatMessage
import org.olymika.chzzkwithme.chat.ChzzkChatType
import org.olymika.chzzkwithme.chat.chatHandler

class ChzzkConfig {
    internal var clientConfig: HttpClientConfig.() -> Unit = {}
    internal var authConfig: AuthConfig.() -> Unit = {}

    var channelId: String? = null

    fun client(config: HttpClientConfig.() -> Unit) {
        clientConfig = config
    }

    fun auth(config: AuthConfig.() -> Unit) {
        authConfig = config
    }
}

class AuthConfig {
    var nidAut: String = ""
    var nidSes: String = ""
}

fun chzzk(
    clientFactory: ChzzkHttpClientFactory? = null,
    config: ChzzkConfig.() -> Unit = {},
): Chzzk {
    val chzzkConfig = ChzzkConfig().apply(config)
    val clientConfig = HttpClientConfig().apply(chzzkConfig.clientConfig)
    val authConfig = AuthConfig().apply(chzzkConfig.authConfig)

    return Chzzk(
        channelId = chzzkConfig.channelId,
        chzzkUserAuth = ChzzkUserAuth(authConfig),
        client = clientFactory?.create(clientConfig) ?: DefaultChzzkHttpClientFactory.create(clientConfig),
        clientConfig = clientConfig,
    )
}

fun main() {
    val chzzk =
        chzzk {
            channelId = "a6c4ddb09cdb160478996007bff35296"

            client {
                loggingLevel = LoggingLevel.ALL
            }

            auth {
                nidAut = "Ad3Wc0WW0RxyNi1wSiFU7L7stFmncJk0t4aIUNRQHpD4M+gDkOejA3nuEPS0QAzY"
                nidSes = "AAABqcLVWYV9Xfafj4aK1dlgMzqpZpoZHOG13yK7O5bCQUNPb/vWdia0ObrYtHUJ9btuZ2tehb6b5MNmo56c6unMpPpcGU9AnjQQTrkdL4GTwd1peL1laN1uvLpqd6+2DrX5X1Iqa7fZbAox076yltReISIhVCdk8xbGX97kECNvNTTsi4xxkuU0uku4ckuryQq3jOYJdYFhPtx6zw4a/FOG8iYR8qUoewzdpKBb6hxMPzXZOkekJx1lG7IxJeaRMy6yg0FvIo5LxkTOTqXog4V4wL6w7X83yJ6XU4vFCa4j0QTWHHvOBXkIwdeq9R8XirPq3GvJxKquCXaleZh4DMOEBm2CM1j8ILX+uEGnvIUV6yRuGm6t7dlcrRyU5yKfD64N0TldDiXmt3ltHC4Sm1x8kdE7Foe8ERTyikKOwqCX1aX5q+6YjrCx8wJUj1QIzqAEftZ1XGNmoW+uOCgu0ll9MK6dEI+0xTxG8NCCWYTRFea7WKqf9AdjDtPLcWDHpzoRJQpuD/J9zBvaqxaa1+V47MB+A+MxSi1NZGC9feDdv0yU1W98eP0x0wf1bU55+7C3ig=="
            }
        }

    runBlocking {
        val chatHandler =
            chatHandler {
                addListener(
                    object : ChzzkChatListener {
                        override suspend fun receive(message: ChzzkChatMessage) {
                            message.chats.forEach {
                                when (it.type) {
                                    ChzzkChatType.NORMAL -> {
                                        println("normal --> ${it.msg}")
                                    }

                                    ChzzkChatType.DONATION -> {
                                        println("donation --> ${it.msg}")
                                    }
                                }
                            }
                        }
                    },
                )

                addListener(
                    object : ChzzkChatListener {
                        override suspend fun receive(message: ChzzkChatMessage) {
                            message.chats.forEach {
                                when (it.type) {
                                    ChzzkChatType.NORMAL -> {
                                        println("normal2 --> ${it.msg}")
                                    }

                                    ChzzkChatType.DONATION -> {
                                        println("donation2 --> ${it.msg}")
                                    }
                                }
                            }
                        }
                    },
                )
            }

        launch { chzzk.chat(chatHandler) }

        // send 가능
        chatHandler.send("ㅋㅋ")
    }
}

/**
 *
 * chzzk {
 *      logging =
 *
 *      client {
 *          logging
 *          ...
 *
 *          support {
 *
 *          }
 *      }
 *
 *      auth {
 *          NID_AUT = ""
 *          NID_SES = ""
 *      }
 *
 *
 * }
 *
 *
 *
 * val chzzkChat = chzzk.chat() {
 *      lisener(
 *          a,
 *          b,
 *          c
 *      )
 * }
 *
 *
 */
