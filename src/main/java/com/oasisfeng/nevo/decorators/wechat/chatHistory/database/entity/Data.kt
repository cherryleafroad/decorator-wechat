package com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity

import android.graphics.drawable.Drawable
import androidx.room.Embedded
import androidx.room.Relation
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.type.ChatType
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.type.MessageType

// special data class for special query, and autoconversion to drawable
// I'm not sure this is faster though, so I won't use it
data class MessageWithAvatarCustom(
    val message: String,
    val message_type: MessageType,
    val chat_type: ChatType,
    val timestamp: Long,
    val group_chat_username: String,
    val avatar: Drawable
)

// this default one will be used
data class MessageWithAvatar(
    @Embedded val message: Message,
    @Relation(
        parentColumn = "user_id",
        entityColumn = "user_id"
    )
    val avatar: Avatar
)

data class UserWithMessageAndAvatar(
    @Embedded val user: User,
    @Relation(
        entity = Message::class,
        parentColumn = "u_id",
        entityColumn = "user_id"
    )
    val data: MessageWithAvatar
)

data class Timestamp(
    val timestamp: Long
)
