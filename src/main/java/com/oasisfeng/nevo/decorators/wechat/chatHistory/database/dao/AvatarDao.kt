package com.oasisfeng.nevo.decorators.wechat.chatHistory.database.dao

import androidx.room.*
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.Avatar
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.User

@Dao
interface AvatarDao {
    @Query("SELECT * FROM avatars WHERE user_id LIKE :user LIMIT 1")
    suspend fun getAvatarUser(user: User): Avatar?

    @Query("SELECT * FROM avatars INNER JOIN users ON users.user_sid LIKE :user_id")
    suspend fun getAvatarFromUserId(user_id: String): Avatar?

    @Query("SELECT * FROM avatars WHERE user_id = :uid")
    suspend fun getAvatarFromUid(uid: Long): Avatar?

    @Update
    suspend fun update(avatar: Avatar)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(avatar: Avatar)

    @Delete
    suspend fun delete(avatar: Avatar)
}
