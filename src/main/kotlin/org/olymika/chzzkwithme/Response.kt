package org.olymika.chzzkwithme

import kotlinx.serialization.Serializable

@Serializable
data class LiveDetailContent(
    val content: ChatChannelId
) {
    @Serializable
    data class ChatChannelId(
        val chatChannelId: String
    )
}

@Serializable
data class AccessTokenContent(
    val content: ChzzkUserToken
) {
    @Serializable
    data class ChzzkUserToken(
        val accessToken: String,
        val extraToken: String
    )
}

@Serializable
data class ChzzkUserContent(
    val content: ChzzkUser
)

@Serializable
data class FollowingChannelContent(
    val content: Content
) {
    @Serializable
    data class Content(
        val followingList: List<FollowingChannel>
    )
}

@Serializable
data class FollowingChannel(
    val channelId: String,
    val channel: Channel,
    val streamer: Streamer,
    val liveInfo: LiveInfo
) {
    @Serializable
    data class Channel(
        val channelName: String,
        val channelImageUrl: String
    )

    @Serializable
    data class Streamer(
        val openLive: Boolean
    )

    @Serializable
    data class LiveInfo(
        val liveTitle: String?,
        val concurrentUserCount: Long,
        val liveCategoryValue: String
    )
}
