package com.oasisfeng.nevo.decorators.wechat.chatHistory.database.dao

import androidx.paging.PagingSource
import androidx.room.*
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.*
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.type.MessageType

@Dao
interface MessageDao {
    @Query("SELECT * FROM messages WHERE user_id LIKE :user_id ORDER BY timestamp ASC")
    suspend fun getAllBySid(user_id: String): MutableList<Message>

    @Query("SELECT * FROM messages WHERE user_id = :uid")
    suspend fun getAllByUid(uid: Long): List<Message>

    @Query("SELECT * FROM messages WHERE user_id LIKE :user_id ORDER BY timestamp DESC")
    suspend fun getAllBySidDesc(user_id: String): MutableList<Message>

    @Query("SELECT * FROM messages WHERE user_id LIKE :user ORDER BY timestamp ASC")
    suspend fun getAllByUser(user: User): MutableList<Message>

    @Query("SELECT * FROM messages WHERE user_id LIKE :user ORDER BY timestamp DESC")
    fun getLatestByUserPaged(user: User): PagingSource<Int, Message>

    @Query("SELECT * FROM messages WHERE user_id = :uid ORDER BY m_id DESC")
    fun getMessagesWithAvatar(uid: Long): PagingSource<Int, MessageWithAvatar>

    @Query("SELECT message, message_type, chat_type, timestamp, group_chat_username, avatars.filename AS avatar " +
            "FROM messages JOIN avatars ON avatars.user_id = messages.user_id " +
            "WHERE messages.user_id = (SELECT u_id FROM users WHERE users.user_sid LIKE :user)")
    fun getMessagesWithAvatarPagedCustom(user: User): PagingSource<Int, MessageWithAvatarCustom>

    @Query("SELECT * FROM messages WHERE user_id LIKE :user ORDER BY timestamp DESC")
    suspend fun getAllByUserDesc(user: User): MutableList<Message>

    @Query("SELECT * FROM messages WHERE user_id LIKE :user_sid ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getAllMessagesBySidLimit(user_sid: String, limit: Int): MutableList<Message>

    @Query("SELECT * FROM messages WHERE user_id LIKE :user ORDER BY timestamp ASC LIMIT :limit")
    suspend fun getAllMessagesByUserLimit(user: User, limit: Int): MutableList<Message>

    @Query("SELECT * FROM messages WHERE user_id = (SELECT u_id FROM users WHERE users.user_sid LIKE :user) ORDER BY m_id DESC LIMIT :limit")
    suspend fun getAllMessagesByUserLimitDesc(user: User, limit: Int): MutableList<Message>

    @Query("SELECT * FROM messages WHERE user_id = :uid AND message_type <> :message_type ORDER BY m_id DESC LIMIT :limit")
    suspend fun getAllMessagesByUserLimitDescNoDateHeader(uid: Long, limit: Int, message_type: MessageType = MessageType.DATE_HEADER): MutableList<Message>

    @Query("SELECT * FROM messages WHERE user_id = (SELECT u_id FROM users WHERE :user_id LIKE :user_id) ORDER BY timestamp DESC LIMIT :limit")
    suspend fun getLatestMessagesByUserLimit(user_id: String, limit: Int): MutableList<Message>

    //@Query("SELECT timestamp FROM messages WHERE user_id = :uid ORDER BY m_id DESC LIMIT :limit")
    //suspend fun getLatestTimestampsByUser(uid: Long, limit: Int): List<Timestamp>

    // get all records INCLUDING and after cetain message type
    @Query("SELECT timestamp FROM messages WHERE m_id >= (SELECT MAX(m_id) FROM messages WHERE user_id = :uid AND message_type = :messageType)")
    suspend fun getLatestTimestampsByUser(uid: Long, messageType: MessageType = MessageType.DATE_HEADER): List<Timestamp>

    @Query("SELECT EXISTS(SELECT 1 FROM messages WHERE timestamp = :timestamp)")
    suspend fun checkTimestampExists(timestamp: Long): Boolean

    @Update
    suspend fun update (message: Message)

    @Update
    suspend fun updateAll (message: List<Message>)

    // timestamp MUST be unique because it's the primary key, but there's a double message insertion bug
    // that tries to insert the same. So just ignore it for now
    // The bug primarily happens when doing dev and reinstalling it (it shows previous conversations)
    // I hope to fix the bug later and remove the need to do this onConflict strategy
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(message: Message)

    @Delete
    suspend fun delete(message: Message)
}
