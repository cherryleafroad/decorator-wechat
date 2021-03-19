package com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "avatars",
    foreignKeys = [ForeignKey(entity = User::class,
        parentColumns = ["u_id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Avatar(
    @PrimaryKey val user_id: Long,
    @ColumnInfo(name = "filename") val filename: String
)
