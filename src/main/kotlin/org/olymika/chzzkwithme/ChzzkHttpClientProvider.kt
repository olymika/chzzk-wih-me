package org.olymika.chzzkwithme

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.request.get
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.serialization.json.Json
import okhttp3.ConnectionPool
import okhttp3.ExperimentalOkHttpApi
import okhttp3.OkHttpClient
import okhttp3.OkHttpClient.*
import okhttp3.coroutines.executeAsync
import okhttp3.logging.HttpLoggingInterceptor
import org.olymika.chzzkwithme.AccessTokenContent.ChzzkUserToken
import org.olymika.chzzkwithme.utils.ChzzkUrlUtils.BASE_URL
import org.olymika.chzzkwithme.utils.ChzzkUrlUtils.CHANNEL_SUFFIX
import org.olymika.chzzkwithme.utils.ChzzkUrlUtils.GAME_API_URL
import org.olymika.chzzkwithme.utils.fromJson
import org.olymika.chzzkwithme.utils.resolveResultOrFailProcess
import java.util.concurrent.TimeUnit

class ChzzkHttpClientDelegator(
    private val candidates: List<ChzzkHttpClientProvider>
) : ChzzkHttpClient {
    override suspend fun getUser(auth: ChzzkUserAuth?): ChzzkUser {
        val supporter = candidates.firstOrNull { it is UserApiSupporter }

        return supporter?.let {
            (supporter as UserApiSupporter).getUser(auth)
        } ?: throw IllegalStateException("Not Found Chzzk Client")
    }

    override suspend fun getChatChannelId(channelId: String): String = candidates.firstOrNull()?.getChatChannelId(channelId) ?: throw IllegalStateException("Not Found Chzzk Client")

    override suspend fun getToken(
        chatChannelId: String,
        auth: ChzzkUserAuth?
    ): ChzzkUserToken = candidates.firstOrNull { it is UserApiSupporter }?.getToken(chatChannelId, auth) ?: throw IllegalStateException("Not Found Chzzk Client")

    override suspend fun getFollowingChannels(
        page: Int,
        size: Long,
        auth: ChzzkUserAuth?
    ): List<FollowingChannel> = candidates.firstOrNull { it is UserApiSupporter }?.getFollowingChannels(page, size, auth) ?: throw IllegalStateException("Not Found Chzzk Client")

    override suspend fun close() {
        candidates.forEach { it.close() }
    }
}

class KtorChzzkHttpClient(
    config: HttpClientConfig
) : ChzzkHttpClientProvider {
    private val client =
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

    override suspend fun getChatChannelId(channelId: String): String =
        client.get("${BASE_URL}${CHANNEL_SUFFIX}/$channelId/live-detail").body<LiveDetailContent>()
            .content
            .chatChannelId

    override suspend fun getToken(
        chatChannelId: String,
        auth: ChzzkUserAuth?
    ): ChzzkUserToken =
        client.get("${GAME_API_URL}/v1/chats/access-token?channelId=$chatChannelId&chatType=STREAMING")
            .body<AccessTokenContent>()
            .content

    override suspend fun getFollowingChannels(
        page: Int,
        size: Long,
        auth: ChzzkUserAuth?
    ): List<FollowingChannel> =
        client.get("${BASE_URL}/service/v1/channels/followings?page=$page&size=$size&sortType=FOLLOW")
            .body<FollowingChannelContent>()
            .content
            .followingList

    override suspend fun close() {
        client.close()
    }
}

class OkHttpChzzkClient(config: HttpClientConfig) : ChzzkHttpClientProvider, UserApiSupporter {
    @OptIn(ExperimentalOkHttpApi::class)
    private val client =
        Builder().apply {
            connectTimeout(config.connectionTimeOut, config.connectionTimeUnit)
            writeTimeout(config.writeTimeOut, config.writeTimeUnit)
            readTimeout(config.readTimeOut, config.readTimeUnit)

            addInterceptor {
                val original = it.request()
                val authorized =
                    original.newBuilder()
                        .addHeader(
                            "User-Agent",
                            "",
                        )
                        .build()

                it.proceed(authorized)
            }

            when (config.loggingLevel) {
                LoggingLevel.NONE -> {}
                LoggingLevel.HEADERS -> {
                    addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.HEADERS })
                }

                LoggingLevel.BODY -> {
                    addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                }

                LoggingLevel.ALL -> {
                    addInterceptor(HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY })
                }
            }

            connectionPool(ConnectionPool(maxIdleConnections = 10))
        }.build()

    private var userClient: OkHttpClient? = null

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getUser(auth: ChzzkUserAuth?): ChzzkUser =
        callWithLazySetUserClient(auth) {
            requireNotNull(userClient) { "The client was not initialized lazily" }
                .newCall(
                    okhttp3.Request.Builder()
                        .url("${GAME_API_URL}/v1/user/getUserStatus")
                        .build(),
                ).executeAsync().use { response ->
                    fromJson<ChzzkUserContent>(response.body.string()).content
                }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getChatChannelId(channelId: String): String =
        client.newCall(
            okhttp3.Request.Builder()
                .url("${BASE_URL}${CHANNEL_SUFFIX}/$channelId/live-detail")
                .build(),
        ).executeAsync().use { response ->
            resolveResultOrFailProcess(response.body.string()) {
                fromJson<LiveDetailContent>(response.body.string())
                    .content
                    .chatChannelId
            }
        }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getToken(
        chatChannelId: String,
        auth: ChzzkUserAuth?
    ): ChzzkUserToken =
        callWithLazySetUserClient(auth) {
            requireNotNull(userClient) { "The client was not initialized lazily" }
                .newCall(
                    okhttp3.Request.Builder()
                        .url("${GAME_API_URL}/v1/chats/access-token?channelId=$chatChannelId&chatType=STREAMING")
                        .build(),
                ).executeAsync().use { response ->
                    fromJson<AccessTokenContent>(response.body.string()).content
                }
        }

    @OptIn(ExperimentalOkHttpApi::class)
    private suspend fun <T> callWithLazySetUserClient(
        auth: ChzzkUserAuth? = null,
        block: suspend () -> T
    ): T {
        if (userClient == null) {
            userClient =
                client.newBuilder().apply {
                    addInterceptor {
                        val original = it.request()
                        val authorized =
                            original.newBuilder()
                                .addHeader(
                                    "Cookie",
                                    "NID_AUT=${auth?.aut}; NID_SES=${auth?.ses}",
                                )
                                .build()

                        it.proceed(authorized)
                    }
                    addInterceptor {
                        val original = it.request()
                        val authorized =
                            original.newBuilder()
                                .addHeader(
                                    "User-Agent",
                                    "",
                                )
                                .build()

                        it.proceed(authorized)
                    }

                    connectionPool(ConnectionPool())
                }.build()
        }

        requireNotNull(userClient) { "The client was not initialized lazily" }
        return block()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    override suspend fun getFollowingChannels(
        page: Int,
        size: Long,
        auth: ChzzkUserAuth?
    ): List<FollowingChannel> {
        return callWithLazySetUserClient(auth) {
            requireNotNull(userClient) { "The client was not initialized lazily" }.newCall(
                okhttp3.Request.Builder().url("${BASE_URL}/service/v1/channels/followings?page=$page&size=$size&sortType=FOLLOW")
                    .build(),
            ).executeAsync().use { response ->
                fromJson<FollowingChannelContent>(response.body.string())
                    .content
                    .followingList
            }
        }
    }

    override suspend fun close() {
        client.connectionPool.evictAll()
        userClient?.connectionPool?.evictAll()
    }
}

object DefaultChzzkHttpClientFactory : ChzzkHttpClientFactory {
    override fun create(config: HttpClientConfig): ChzzkHttpClient {
        return listOf(
            KtorChzzkHttpClient(config),
            OkHttpChzzkClient(config),
        ).let {
            ChzzkHttpClientDelegator(it)
        }
    }
}

interface ChzzkHttpClientFactory {
    fun create(config: HttpClientConfig): ChzzkHttpClient
}

class HttpClientConfig {
    var loggingLevel: LoggingLevel = LoggingLevel.NONE
    var connectionTimeOut: Long = 10_000
    var connectionTimeUnit: TimeUnit = TimeUnit.SECONDS
    var writeTimeOut: Long = 30
    var writeTimeUnit: TimeUnit = TimeUnit.SECONDS
    var readTimeOut: Long = 30
    var readTimeUnit: TimeUnit = TimeUnit.SECONDS
}

enum class LoggingLevel {
    NONE,
    HEADERS,
    BODY,
    ALL
}
