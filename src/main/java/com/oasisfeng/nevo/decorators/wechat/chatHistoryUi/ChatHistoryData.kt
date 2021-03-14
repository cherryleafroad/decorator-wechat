package com.oasisfeng.nevo.decorators.wechat.chatHistoryUi

import android.graphics.drawable.Drawable
import android.util.ArrayMap
import androidx.paging.PagingSource
import androidx.room.*
import androidx.room.ForeignKey.CASCADE
import java.sql.Time

enum class MessageType {
    RECEIVER,
    SENDER,
    RECALLED,
    DATE_HEADER
}

enum class ChatType {
    CHAT,
    GROUP
}


@Entity(tableName = "users",
    indices = [
        Index(value = ["user_sid"], unique = true)
    ]
)
data class User(
    @ColumnInfo(name = "user_sid") val user_sid: String,
    @ColumnInfo(name = "username") var username: String,
    @ColumnInfo(name = "latest_message") var latest_message: Long = 0,
    @PrimaryKey(autoGenerate = true) var u_id: Long = 0
)


@Entity(tableName = "messages",
    foreignKeys = [ForeignKey(entity = User::class,
        parentColumns = ["u_id"],
        childColumns = ["user_id"],
        onDelete = CASCADE)],
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


@Entity(tableName = "avatars",
    foreignKeys = [ForeignKey(entity = User::class,
        parentColumns = ["u_id"],
        childColumns = ["user_id"],
        onDelete = CASCADE)]
)
data class Avatar(
    @PrimaryKey val user_id: Long,
    @ColumnInfo(name = "filename") val filename: String
)

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
    fun getMessagesWithAvatarPaged(uid: Long): PagingSource<Int, MessageWithAvatar>

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

@Database(entities = [User::class, Message::class, Avatar::class], version = 2, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun avatarDao(): AvatarDao
}

class Converters {
    @TypeConverter
    fun fromUser(user: User): String {
        return user.user_sid
    }

    @TypeConverter
    fun toMessageType(value: Int): MessageType {
        return enumValues<MessageType>()[value]
    }

    @TypeConverter
    fun fromMessageType(value: MessageType): Int {
        return value.ordinal
    }

    @TypeConverter
    fun toChatType(value: Int): ChatType {
        return enumValues<ChatType>()[value]
    }

    @TypeConverter
    fun fromChatType(value: ChatType): Int {
        return value.ordinal
    }

    // needs to check if the string is empty or not,
    // could return null. Not important cause we're not using it though
    @TypeConverter
    fun toDrawable(value: String): Drawable {
        return Drawable.createFromPath(value)!!
    }
}

