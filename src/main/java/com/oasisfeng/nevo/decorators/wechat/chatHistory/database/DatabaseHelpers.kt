package com.oasisfeng.nevo.decorators.wechat.chatHistory.database

import android.app.RemoteInput
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.ArrayMap
import com.oasisfeng.nevo.decorators.wechat.*
import com.oasisfeng.nevo.decorators.wechat.chatHistory.DateConverter
import com.oasisfeng.nevo.decorators.wechat.chatHistory.ReplyIntent
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.Avatar
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.Message
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.User
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.type.ChatType
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.type.MessageType
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.type.UserType
import com.oasisfeng.nevo.decorators.wechat.chatHistory.fragment.UserListFragment.Companion.ACTION_NOTIFY_USER_CHANGE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object DatabaseHelpers {
    private var avatarMap = ArrayMap<String, Bitmap>()
    suspend fun checkAndInsertDateHeader(context: Context, uid: Long, chatType: ChatType, timestamp: Long) {
        val db = AppDatabase.get(context.applicationContext)
        val latestMessages = db.messageDao().getLatestTimestampsByUser(uid)

        if (latestMessages.isNotEmpty()) {
            if (DateConverter.shouldInsertDateHeader(latestMessages, timestamp)) {
                val message = Message(
                    uid,
                    "",
                    MessageType.DATE_HEADER,
                    chatType,
                    timestamp
                )
                db.messageDao().insert(message)
            }
        } else {
            // null record, always insert date
            val message = Message(
                uid,
                "",
                MessageType.DATE_HEADER,
                chatType,
                timestamp
            )
            db.messageDao().insert(message)
        }
    }

    @JvmStatic
    fun addReplyServiceIntent(context: Context, userSid: String, on_reply: Intent, remoteInput: RemoteInput) {
        GlobalScope.launch(Dispatchers.IO) {
            val app = (context.applicationContext as WeChatApp)
            val id = getUserUid(context, userSid)!!

            // store pending intent into queue for when we open UI to receive them
            val replyData = ReplyIntent(
                id,
                on_reply,
                remoteInput
            )

            app.replyMap[id] = replyData
            val isRunning = app.isMessengerServiceRunning
            if (isRunning) {
                app.replyIntentEvent.postValue(replyData)
            }
        }
    }

    private suspend fun getUserUid(context: Context, userSid: String): Long? {
        val db = AppDatabase.get(context.applicationContext)
        return db.userDao().getUidFromUserId(userSid)
    }

    @JvmStatic
    fun addReply(context: Context, id: String, isChat: Boolean, reply: String) {
        val db = AppDatabase.get(context.applicationContext)

        GlobalScope.launch(Dispatchers.IO) {
            val chatType = if (isChat) ChatType.CHAT else ChatType.GROUP

            val user = db.userDao().findByUserId(id)
            if (user != null) {
                val now = System.currentTimeMillis()
                val translated = EmojiTranslator.extractExtras(reply)
                val message = Message(user.u_id, translated, MessageType.SENDER, chatType, now)
                db.messageDao().insert(message)
                
                user.latest_message = now
                db.userDao().update(user)

                // notify userlist of new reply
                val intent = Intent(ACTION_NOTIFY_USER_CHANGE)
                intent.setPackage(context.packageName)
                context.sendBroadcast(intent)
            }
        }
    }

    @JvmStatic
    fun checkAndUpdateAvatar(context: Context, key: String, id: String, avatar: Bitmap) {
        val db = AppDatabase.get(context.applicationContext)

        GlobalScope.launch(Dispatchers.IO) {
            // fill it in if it's empty
            val av = db.avatarDao().getAvatarFromUserId(id)
            if (av == null || av.filename.isEmpty()) {
                saveAvatar(context, id, key, avatar, db)
                avatarMap[key] = avatar
                return@launch
            }

            if (avatarMap.contains(key)) {
                if (!avatar.sameAs(avatarMap[key])) {
                    saveAvatar(context, id, key, avatar, db)
                }
            } else {
                val hash = id.hashCode()
                val file = File(context.cacheDir, "$hash.png")

                if (file.exists()) {
                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    if (!avatar.sameAs(bitmap)) {
                        saveAvatar(context, id, key, avatar, db)
                    } else {
                        avatarMap[key] = bitmap
                        val uid = db.userDao().getUidFromUserId(id)
                        if (uid != null) {
                            db.avatarDao().insert(Avatar(uid, file.absolutePath))
                        }
                    }
                } else {
                    saveAvatar(context, id, key, avatar, db)
                }
            }
        }
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private suspend fun saveAvatar(context: Context, id: String, key: String, avatar: Bitmap, db: AppDatabase) {
        val hash = id.hashCode()

        val file = File(context.cacheDir, "$hash.png")

        try {
            val out = FileOutputStream(file)
            avatar.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()

            val uid = db.userDao().getUidFromUserId(id)
            if (uid != null) {
                db.avatarDao().insert(Avatar(uid, file.absolutePath))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

        avatarMap[key] = avatar
    }

    fun prepopulateDatabase(db: AppDatabase) {
        GlobalScope.launch(Dispatchers.IO) {
            // wxid's are pretty much always random, there's no way one will be named this
            val userYou = User(
                "wxid_user_you",
                "You",
                0,
                UserType.YOU
            )

            val uid = db.userDao().insert(userYou)

            val avatar = Avatar(
                uid,
                ""
            )

            db.avatarDao().insert(avatar)
        }
    }
}
