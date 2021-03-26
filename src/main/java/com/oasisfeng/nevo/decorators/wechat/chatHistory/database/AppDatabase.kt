package com.oasisfeng.nevo.decorators.wechat.chatHistory.database

import android.app.Application
import android.content.Context
import android.graphics.drawable.Drawable
import androidx.room.*
import androidx.sqlite.db.SupportSQLiteDatabase
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.dao.AvatarDao
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.dao.DraftDao
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.dao.MessageDao
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.dao.UserDao
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.Avatar
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.Draft
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.Message
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.User
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.type.ChatType
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.type.MessageType
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.type.UserType

private object AppDatabaseInstance {
    private lateinit var instance: AppDatabase

    fun get(application: Application): AppDatabase {
        return get(application.applicationContext)
    }

    fun get(context: Context): AppDatabase {
        if (!this::instance.isInitialized) {
            instance = Room.databaseBuilder(
                context,
                AppDatabase::class.java, "messages"
            )
                // prepop database with first initialiation info
                .addCallback(object : RoomDatabase.Callback() {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        super.onCreate(db)
                        DatabaseHelpers.prepopulateDatabase(AppDatabase.get(context))
                    }
                })
                .enableMultiInstanceInvalidation()
                .build()
        }

        return instance
    }
}

@Database(entities = [User::class, Message::class, Avatar::class, Draft::class], version = 5, exportSchema = false)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        fun get(application: Application): AppDatabase {
            return AppDatabaseInstance.get(application)
        }

        fun get(context: Context): AppDatabase {
            return AppDatabaseInstance.get(context)
        }
    }

    abstract fun userDao(): UserDao
    abstract fun messageDao(): MessageDao
    abstract fun avatarDao(): AvatarDao
    abstract fun draftDao(): DraftDao
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

    @TypeConverter
    fun toUserType(value: Int): UserType {
        return enumValues<UserType>()[value]
    }

    @TypeConverter
    fun fromUserType(value: UserType): Int {
        return value.ordinal
    }

    // needs to check if the string is empty or not,
    // could return null. Not important cause we're not using it though
    @TypeConverter
    fun toDrawable(value: String): Drawable {
        return Drawable.createFromPath(value)!!
    }
}
