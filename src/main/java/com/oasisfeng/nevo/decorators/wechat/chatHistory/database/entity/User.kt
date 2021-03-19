package com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "users",
    indices = [
        Index(value = ["user_sid"], unique = true),
        Index(value = ["latest_message"])
    ]
)
data class User(
    @ColumnInfo(name = "user_sid") val user_sid: String,
    @ColumnInfo(name = "username") var username: String,
    @ColumnInfo(name = "latest_message") var latest_message: Long = 0,
    @PrimaryKey(autoGenerate = true) var u_id: Long = 0
)
