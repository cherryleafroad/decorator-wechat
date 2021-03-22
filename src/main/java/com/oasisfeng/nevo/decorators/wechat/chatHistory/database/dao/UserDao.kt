package com.oasisfeng.nevo.decorators.wechat.chatHistory.database.dao

import androidx.room.*
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.User
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.UserWithMessageAndAvatar

@Dao
interface UserDao {
    @Query("SELECT * FROM users")
    fun getAll(): List<User>

    @Transaction
    @Query("SELECT * FROM users ORDER BY latest_message DESC")
    suspend fun getUsersWithMessageAndAvatar(): List<UserWithMessageAndAvatar>

    @Query("SELECT * FROM users WHERE user_sid IN (:user_ids)")
    suspend fun getAllByUserIds(vararg user_ids: String): List<User>

    @Query("SELECT * FROM users WHERE username IN (:usernames)")
    suspend fun getAllByUsernames(vararg usernames: String): List<User>

    @Query("SELECT * FROM users WHERE username LIKE :username LIMIT 1")
    suspend fun findByUsername(username: String): User?

    @Query("SELECT * FROM users WHERE user_sid LIKE :user_id LIMIT 1")
    suspend fun findByUserId(user_id: String): User?

    @Query("UPDATE users SET username = :username WHERE user_sid LIKE :user")
    suspend fun updateUsername(user: User, username: String)

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getLatestUser(): User

    @Query("SELECT u_id FROM users WHERE user_sid LIKE :user_id")
    suspend fun getUidFromUserId(user_id: String): Long?

    @Query("SELECT user_sid FROM users WHERE u_id = :uid")
    suspend fun getSidFromUid(uid: Long): String

    @Update
    suspend fun update(user: User)

    @Insert
    suspend fun insert(user: User): Long

    @Query("DELETE FROM users")
    suspend fun deleteAll()

    @Query("DELETE FROM users WHERE user_sid LIKE :user_id")
    suspend fun deleteUserByUserId(user_id: String)

    @Query("DELETE FROM users WHERE user_sid LIKE :user")
    suspend fun deleteUser(user: User)

    @Query("DELETE FROM users WHERE u_id = :uid")
    suspend fun deleteByUid(uid: Long)

    @Delete
    suspend fun delete(user: User)
}
