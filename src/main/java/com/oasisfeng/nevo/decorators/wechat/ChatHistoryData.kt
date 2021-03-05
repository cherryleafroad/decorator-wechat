package com.oasisfeng.nevo.decorators.wechat

import androidx.room.*

@Entity
data class User(
    @PrimaryKey val sid: String,
    @ColumnInfo(name = "username") var username: String
)

@Entity
data class Message(
    @ColumnInfo(name = "user_sid") val user_sid: String,
    @ColumnInfo(name = "message") var message: String,
    @ColumnInfo(name = "is_self") var is_reply: Boolean = false,
    @PrimaryKey(autoGenerate = true) val id: Long = 0
)

@Dao
interface UserDao {
    @Query("SELECT * FROM user")
    suspend fun getAll(): List<User?>

    @Query("SELECT * FROM user WHERE sid IN (:sids)")
    suspend fun getAllBySids(vararg sids: String): List<User?>

    @Query("SELECT * FROM user WHERE username IN (:usernames)")
    suspend fun getAllByUsernames(vararg usernames: String): List<User?>

    @Query("SELECT * FROM user WHERE username LIKE :username LIMIT 1")
    suspend fun findByUsername(username: String): User?

    @Query("SELECT * FROM user WHERE sid LIKE :sid LIMIT 1")
    suspend fun findBySid(sid: String): User?

    @Query("UPDATE user SET username = :username WHERE sid LIKE :user")
    suspend fun updateUsername(user: User, username: String)

    @Update
    suspend fun update(user: User)

    @Insert
    suspend fun insert(user: User)

    @Query("DELETE FROM user")
    suspend fun deleteAll()

    @Query("DELETE FROM user WHERE sid LIKE :sid")
    suspend fun deleteUserBySid(sid: String)

    @Query("DELETE FROM user WHERE sid LIKE :user")
    suspend fun deleteUser(user: User)

    @Delete
    suspend fun delete(user: User)
}

@Dao
interface MessageDao {
    @Query("SELECT * FROM message WHERE user_sid LIKE :user_sid")
    suspend fun getAllBySid(user_sid: String): MutableList<Message?>

    @Query("SELECT * FROM message WHERE user_sid LIKE :user_sid ORDER BY id ASC")
    suspend fun getAllBySidAsc(user_sid: String): MutableList<Message?>

    @Query("SELECT * FROM message WHERE user_sid LIKE :user_sid ORDER BY id DESC")
    suspend fun getAllBySidDesc(user_sid: String): MutableList<Message?>

    @Query("SELECT * FROM message WHERE user_sid LIKE :user")
    suspend fun getAllByUser(user: User): MutableList<Message?>

    @Query("SELECT * FROM message WHERE user_sid LIKE :user ORDER BY id ASC")
    suspend fun getAllByUserAsc(user: User): MutableList<Message?>

    @Query("SELECT * FROM message WHERE user_sid LIKE :user ORDER BY id DESC")
    suspend fun getAllByUserDesc(user: User): MutableList<Message?>

    @Query("SELECT * FROM message WHERE user_sid LIKE :user_sid LIMIT :limit")
    suspend fun getAllMessagesBySidLimit(user_sid: String, limit: Int): MutableList<Message?>

    @Query("SELECT * FROM message WHERE user_sid LIKE :user LIMIT :limit")
    suspend fun getAllMessagesByUserLimit(user: User, limit: Int): MutableList<Message?>

    @Query("SELECT * FROM message WHERE user_sid LIKE :user ORDER BY id ASC LIMIT :limit")
    suspend fun getAllMessagesByUserLimitAsc(user: User, limit: Int): MutableList<Message?>

    @Query("SELECT * FROM message WHERE user_sid LIKE :user ORDER BY id DESC LIMIT :limit")
    suspend fun getAllMessagesByUserLimitDesc(user: User, limit: Int): MutableList<Message?>

    @Update
    suspend fun update (message: Message)

    @Update
    suspend fun updateAll (messages: List<Message>)

    @Insert
    suspend fun insert(message: Message)

    @Query("DELETE FROM message WHERE user_sid LIKE :user")
    suspend fun deleteAllByUser(user: User)

    @Query("DELETE FROM message WHERE user_sid LIKE :user_sid")
    suspend fun deleteAllBySid(user_sid: String)

    @Query("DELETE FROM message")
    suspend fun deleteAll()

    @Delete
    suspend fun delete(message: Message)
}

@Database(entities = [User::class, Message::class], version = 1, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
}

class Converters {
    @TypeConverter
    fun fromUser(user: User): String {
        return user.sid
    }
}
