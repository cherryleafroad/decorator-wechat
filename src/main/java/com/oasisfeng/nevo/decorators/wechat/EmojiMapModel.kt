package com.oasisfeng.nevo.decorators.wechat

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Emojis(
    val config: Config,
    val emoji: List<Emoji>
)

@Serializable
data class Config(
    val boundary: String
)

@Suppress("PropertyName")
@Serializable
class Emoji {
    val id: String = "0"
    val keys: List<String> = listOf()
    val emoji: String? = null
    val enabled: Boolean = true
    @SerialName("enabled-chat")
    val enabled_chat: Boolean = true
    @SerialName("title-emoji")
    val title_emoji: String? = null
    @SerialName("chat-title-emoji")
    val chat_title_emoji: String?  =null

    @SerialName("emoji-compat")
    val emoji_compat: String? = null
    @SerialName("compat-with")
    val compat_with: Int? = null
}
