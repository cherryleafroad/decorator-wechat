package com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "drafts",
    foreignKeys = [ForeignKey(entity = User::class,
        parentColumns = ["u_id"],
        childColumns = ["user_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Draft(
    @PrimaryKey val user_id: Long,
    @ColumnInfo(name = "message") var message: String
)
