
# Chzzk with Me
> [!CAUTION]
> We are aware that the current implementation is not functioning properly as Chzzk has now officially released their [public API](https://chzzk.gitbook.io/chzzk). We are planning to support the official API in our upcoming updates.
Thank you for your patience while we work on integrating the new official Chzzk API. We appreciate your understanding during this transition period.
Please stay tuned for updates.

chzzk with me is an unofficial library that allows you to integrate with [Naver Chzzk](https://chzzk.naver.com/) API.

First add the dependency to your project:
```kotlin
repositories {
    mavenCentral()
}

implementation("org.olymika:chzzk-with-me:0.0.6")
```

## Chzzk API
When using API, you can write the code as shown below.

You can set `auth`. <br>
Replace `NID_AUT` and `NID_SES` with the values from the `chzzk` cookies


### Chat Channel ID API
```kotlin
// Initialize the Chzzk client with channel ID
val chzzk = chzzk {
    channelId = "c7ded8ea6b0605d3c78e18650d2df83b"
}

runBlocking {
    val chatChannelId = chzzk.getChatChannelId()
}
```

### Chat Message API
```kotlin
val chzzk = chzzk {
    channelId = "c7ded8ea6b0605d3c78e18650d2df83b"
}

runBlocking {
    val chzzkUser = chzzk.getUser()
}
```
If user information is needed, you can add and retrieve `auth`
```kotlin
val chzzk = chzzk {
    channelId = "c7ded8ea6b0605d3c78e18650d2df83b"

    auth {
        nidAut = "NID_AUT"
        nidSes = "NID_SES"
    }
}

runBlocking {
    val chzzkUser = chzzk.getUser()
}
```

### Token API
If `auth` is not set, this is a token for an unauthenticated user
```kotlin
val chzzk = chzzk {
        channelId = "a6c4ddb09cdb160478996007bff35296"


        auth {
            nidAut = "NID_AUT"
            nidSes = "NID_SES"
        }
    }

runBlocking {
    val chatChannelId = chzzk.getChatChannelId()

    chzzk.getToken(chatChannelId)
}
```

### Following Channels API
```kotlin
val chzzk = chzzk {
    channelId = "c7ded8ea6b0605d3c78e18650d2df83b"

    auth {
        nidAut = "NID_AUT"
        nidSes = "NID_SES"
    }
}

runBlocking {
    val followingChannels = chzzk.getFollowingChannels()
}
```
- - -

## Chzzk Chat
When using Chat, you can write the code as shown below.
```kotlin
val chzzk = chzzk {
    channelId = "c7ded8ea6b0605d3c78e18650d2df83b"
}

// Set up a chat handler to process incoming messages
val handler: ChzzkChatHandler = chatHandler {

    // You can add multiple listeners using `addListener`
    addListener(object : ChzzkChatListener {
        override suspend fun receive(message: ChzzkChatMessage) {
            message.chats.forEach {
                when (it.type) {
                    ChzzkChatType.DONATION ->
                        println("Donation: ${it.msg}")
                    ChzzkChatType.NORMAL ->
                        println("Normal: ${it.msg}")
                }
            }
        }
    })
}

// Start the chat client in a coroutine
runBlocking {
    launch { chzzk.chat(handler) }
}
```

The chat message contains the profile information of the message user. <br>
You can obtain the nickname and profileImageUrl from the Profile of ChzzkChatMessage.
```kotlin
val handler: ChzzkChatHandler = chatHandler {

    // You can add multiple listeners using `addListener`
    addListener(object : ChzzkChatListener {
        override suspend fun receive(message: ChzzkChatMessage) {
            message.chats.forEach {
                when (it.type) {
                    ChzzkChatType.DONATION -> {
                        it.profile?.let { profile ->
                            println("nickname: ${profile.nickname}")
                            println("profileImageUrl: ${profile.profileImageUrl}")
                        }
                    }

                    ChzzkChatType.NORMAL -> {
                        it.profile?.let { profile ->
                            println("nickname: ${profile.nickname}")
                            println("profileImageUrl: ${profile.profileImageUrl}")
                        }
                    }
                }
            }
        }
    })
}
```

If it's a donation, you can find out how much cheese was delivered through the extra field.
```kotlin
val handler = chatHandler {
    addListener(object : ChzzkChatListener {
        override suspend fun receive(message: ChzzkChatMessage) {
            message.chats.forEach {
                when(it.type) {
                    ChzzkChatType.DONATION -> {
                        it.extras?.let { extras ->
                            println("Donation is Anonymous: ${extras.isAnonymous}")
                            println("Donation nickname : ${extras.nickname}")
                            println("Donation amount : ${extras.payAmount}")
                        }
                    }
                    ChzzkChatType.NORMAL -> {}
                }
            }
        }
    })
}
```

### Sending a message
```kotlin
val chzzk = chzzk {
    channelId = "c7ded8ea6b0605d3c78e18650d2df83b"

    // To send a message, you need to set `NID_AUT` and `NID_SES` from the cookies.
    auth {
        nidAut = "NID_AUT"
        nidSes = "NID_SES"
    }
}

val handler: ChzzkChatHandler = chatHandler {
    addListener(object : ChzzkChatListener {
        override suspend fun receive(message: ChzzkChatMessage) {
            message.chats.forEach {
                when (it.type) {
                    ChzzkChatType.DONATION ->
                        println("Donation: ${it.msg}")
                    ChzzkChatType.NORMAL ->
                        println("Normal: ${it.msg}")
                }
            }
        }
    })
}

runBlocking {
    launch { chzzk.chat(handler) }
}

// Send a chat message
handler.send("message")
```

### Chat Close
When closing Chat, you can use the handler’s close method as shown below
```kotlin
val chzzk = chzzk {
    channelId = "c7ded8ea6b0605d3c78e18650d2df83b"

    auth {
        nidAut = "NID_AUT"
        nidSes = "NID_SES"
    }
}


val handler: ChzzkChatHandler = chatHandler {
    addListener(object : ChzzkChatListener {
        override suspend fun receive(message: ChzzkChatMessage) {
            message.chats.forEach {
                when (it.type) {
                    ChzzkChatType.DONATION ->
                        println("Donation: ${it.msg}")
                    ChzzkChatType.NORMAL ->
                        println("Normal: ${it.msg}")
                }
            }
        }
    })
}

runBlocking {
    launch { chzzk.chat(handler) }
}

// Close the chat
handler.close()
```
