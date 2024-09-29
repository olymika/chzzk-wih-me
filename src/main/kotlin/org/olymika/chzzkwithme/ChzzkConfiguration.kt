package org.olymika.chzzkwithme

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
    config: ChzzkConfig.() -> Unit = {}
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
