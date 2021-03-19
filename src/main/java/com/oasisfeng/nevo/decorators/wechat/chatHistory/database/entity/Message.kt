package com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity

import androidx.room.*
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.type.ChatType
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.type.MessageType

@Entity(tableName = "messages",
    foreignKeys = [ForeignKey(entity = User::class,
        parentColumns = ["u_id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    )],
    indices = [
        // ordinarily timestamp is unique, but our date headers have the same timestamp
        Index(value = ["timestamp"]),
        Index(value = ["user_id"])
    ]
)
data class Message(
    @ColumnInfo(name = "user_id") val user_id: Long,
    @ColumnInfo(name = "message") var message: String,
    @ColumnInfo(name = "message_type") var message_type: MessageType,
    @ColumnInfo(name = "chat_type") val chat_type: ChatType,
    @ColumnInfo(name = "timestamp") val timestamp: Long,
    @ColumnInfo(name = "group_chat_username") val group_chat_username: String = "",
    @PrimaryKey(autoGenerate = true) val m_id: Long = 0
)
