package org.olymika.chzzkwithme

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertAll
import org.olymika.chzzkwithme.TestChannel.HAN_DONG_SUK
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class ChzzkApiTest {
    @Test
    fun testGetChannelIdApi() {
        val chzzk = chzzk { channelId = HAN_DONG_SUK }

        val chatChannelId =
            runBlocking {
                chzzk.getChatChannelId()
            }

        assertNotNull(chatChannelId)
    }

    @Test
    fun testGetAnonymousUserApi() {
        val chzzk =
            chzzk {
                channelId = HAN_DONG_SUK
            }

        val user =
            runBlocking {
                chzzk.getUser()
            }

        assertNull(user.userIdHash)
    }

    @Test
    fun testGetAnonymousUserTokenApi() {
        val chzzk =
            chzzk {
                channelId = HAN_DONG_SUK
            }

        val userToken =
            runBlocking {
                val chatChannelId = chzzk.getChatChannelId()
                chzzk.getToken(chatChannelId)
            }

        assertAll(
            { assertNotNull(userToken.extraToken) },
            { assertNotNull(userToken.accessToken) }
        )
    }
}
