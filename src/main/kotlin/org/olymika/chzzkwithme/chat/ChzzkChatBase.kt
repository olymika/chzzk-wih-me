package org.olymika.chzzkwithme.chat

import org.olymika.chzzkwithme.utils.fromJson
import java.math.BigDecimal

abstract class ChzzkChatBase<T>(
    val cid: String = "",
    val svcid: String = "game",
    val ver: String = "3",
    val cmd: Int = ChzzkMesssageType.Commands.CONNECT,
    val bdy: T,
    val tid: Int = 1
)

class ChzzkChatOpenBase(
    cid: String,
    svcid: String = "game",
    ver: String = "3",
    cmd: Int = ChzzkMesssageType.Commands.CONNECT,
    bdy: Body,
    tid: Int = 1
) : ChzzkChatBase<ChzzkChatOpenBase.Body>(
        cid = cid,
        svcid = svcid,
        ver = ver,
        cmd = cmd,
        bdy = bdy,
        tid = tid,
    ) {
    data class Body(
        val accTkn: String,
        val auth: String,
        val devType: Int = 2001,
        val devName: String = "ChzzkWithMe/1.0.0",
        val libVer: String = "1.0.0",
        val osVer: String = "macOs/10.15.7",
        val locale: String = "ko",
        val timezone: String = "Asia/Seoul",
        val uid: String?
    )
}

data class ChzzkChatBoundPing(
    var cmd: Int = ChzzkMesssageType.Commands.PING,
    var ver: String = "2"
)

data class ChzzkChatBoundPong(
    val cmd: Int = ChzzkMesssageType.Commands.PONG,
    val ver: String = "2"
)

class ChzzkSendChatBase(
    cid: String,
    svcid: String = "game",
    ver: String = "3",
    cmd: Int = ChzzkMesssageType.Commands.SEND_CHAT,
    bdy: Body,
    tid: Int = 1,
    val sid: String,
    val retry: Boolean = false,
    val msgTime: Long = System.currentTimeMillis(),
    val msgTypCode: Int = ChzzkMesssageType.ChatTypes.TEXT
) : ChzzkChatBase<ChzzkSendChatBase.Body>(
        cid = cid,
        svcid = svcid,
        ver = ver,
        cmd = cmd,
        bdy = bdy,
        tid = tid,
    ) {
    data class Extras(
        val chatType: String = "STREAMING",
        val osType: String = "PC",
        val extraToken: String,
        val streamChannelId: String,
        val emojis: String = ""
    )

    data class Body(
        val extras: String,
        val msg: String,
        val msgTime: Long = System.currentTimeMillis(),
        val msgTypCode: Int = ChzzkMesssageType.ChatTypes.TEXT
    )
}

class ChzzkChatConnectBase(
    cid: String,
    svcid: String = "game",
    ver: String = "3",
    cmd: Int = ChzzkMesssageType.Commands.SEND_CHAT,
    bdy: Body,
    tid: Int = 1,
    val retCode: Int,
    val retMsg: String
) : ChzzkChatBase<ChzzkChatConnectBase.Body>(
        cid = cid,
        svcid = svcid,
        ver = ver,
        cmd = cmd,
        bdy = bdy,
        tid = tid,
    ) {
    data class Body(
        val sid: String
    )
}

class ChzzkRecentChatBase(
    cid: String,
    svcid: String = "game",
    ver: String = "3",
    cmd: Int = ChzzkMesssageType.Commands.SEND_CHAT,
    bdy: Body,
    tid: Int = 1
) : ChzzkChatBase<ChzzkRecentChatBase.Body>(
        cid = cid,
        svcid = svcid,
        ver = ver,
        cmd = cmd,
        bdy = bdy,
        tid = tid,
    ) {
    data class Body(
        val messageList: List<RecentChat>,
        val userCount: Int
    )

    data class RecentChat(
        val userId: String,
        val content: String,
        val messageTypeCode: Int,
        val createTime: Long,
        val extras: String,
        val profile: String
    )
}

data class ChzzkChatReceiveMessage(
    val cid: String,
    val svcid: String,
    val ver: String,
    val cmd: Int,
    val bdy: List<Body>,
    val tid: String? = null
) {
    data class Body(
        val profile: String? = null,
        val msg: String,
        val extras: String? = null
    )

    fun toChatMessage(type: ChzzkChatType): ChzzkChatMessage =
        bdy.map {
            when (type) {
                ChzzkChatType.NORMAL ->
                    ChzzkChatMessage.Chat(
                        profile = it.profile?.let { profile -> fromJson<ChzzkChatProfile>(profile) },
                        msg = it.msg,
                        type = type,
                    )

                ChzzkChatType.DONATION ->
                    ChzzkChatMessage.Chat(
                        profile = it.profile?.let { profile -> fromJson<ChzzkChatProfile>(profile) },
                        msg = it.msg,
                        type = type,
                        extras = it.extras?.let { extras -> fromJson<ChzzkChatDonationExtras>(extras) },
                    )
            }
        }.let { ChzzkChatMessage(it) }
}

data class ChzzkChatMessage(
    val chats: List<Chat>
) {
    data class Chat(
        val profile: ChzzkChatProfile?,
        val msg: String,
        val type: ChzzkChatType,
        val extras: ChzzkChatDonationExtras? = null
    )
}

enum class ChzzkChatType {
    NORMAL,
    DONATION
}

data class ChzzkChatProfile(
    val nickname: String,
    val profileImageUrl: String?
)

data class ChzzkChatDonationExtras(
    val isAnonymous: Boolean,
    val payAmount: BigDecimal?,
    val nickname: String?
)

internal class ChzzkMesssageType {
    internal object Commands {
        const val PING: Int = 0
        const val PONG: Int = 10000
        const val CONNECT: Int = 100
        const val CONNECTED: Int = 10100
        const val REQUEST_RECENT_CHAT: Int = 5101
        const val RECENT_CHAT: Int = 15101
        const val EVENT: Int = 93006
        const val CHAT: Int = 93101
        const val DONATION: Int = 93102
        const val KICK: Int = 94005
        const val BLOCK: Int = 94006
        const val BLIND: Int = 94008
        const val NOTICE: Int = 94010
        const val PENALTY: Int = 94015
        const val SEND_CHAT: Int = 3101
    }

    internal object ChatTypes {
        const val TEXT: Int = 1
        const val IMAGE: Int = 2
        const val STICKER: Int = 3
        const val VIDEO: Int = 4
        const val RICH: Int = 5
        const val DONATION: Int = 10
        const val SUBSCRIPTION: Int = 11
        const val SYSTEM_MESSAGE: Int = 30
    }
}
