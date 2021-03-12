package com.oasisfeng.nevo.decorators.wechat.chatHistoryUi

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.ArrayMap
import com.oasisfeng.nevo.decorators.wechat.*
import com.oasisfeng.nevo.decorators.wechat.chatHistoryUi.ChatHistoryActivity.Companion.EXTRA_USER_ID
import com.oasisfeng.nevo.decorators.wechat.chatHistoryUi.UserActivity.Companion.ACTION_NOTIFY_USER_CHANGE
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object DatabaseHelpers {
    var avatarMap = ArrayMap<String, Bitmap>()

    @JvmStatic
    fun addReply(context: Context, id: String, isChat: Boolean, reply: String, timestamp: Long) {
        val db = ((context.applicationContext as WeChatApp)).db

        GlobalScope.launch(Dispatchers.IO) {
            val chatType = if (isChat) ChatType.CHAT else ChatType.GROUP

            val user = db.userDao().findByUserId(id)!!
            user.latest_message = timestamp
            val message = Message(user.u_id, reply, MessageType.SENDER, chatType, System.currentTimeMillis())
            db.messageDao().insert(message)
            db.userDao().update(user)

            // notify userlist of new reply
            val intent = Intent(ACTION_NOTIFY_USER_CHANGE)
            intent.putExtra(EXTRA_USER_ID, user.u_id)
            intent.setPackage(context.packageName)
            context.sendBroadcast(intent)
        }
    }

    @JvmStatic
    fun checkAndUpdateAvatar(context: Context, key: String, id: String, avatar: Bitmap) {
        val db = ((context.applicationContext as WeChatApp)).db

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
                        val uid = db.userDao().getUidFromUserId(id)!!
                        db.avatarDao().insert(Avatar(uid, file.absolutePath))
                    }
                } else {
                    saveAvatar(context, id, key, avatar, db)
                }
            }
        }
    }

    private suspend fun saveAvatar(context: Context, id: String, key: String, avatar: Bitmap, db: AppDatabase) {
        val hash = id.hashCode()

        val file = File(context.cacheDir, "$hash.png")

        try {
            val out = FileOutputStream(file)
            avatar.compress(Bitmap.CompressFormat.PNG, 100, out)
            out.flush()
            out.close()

            val uid = db.userDao().getUidFromUserId(id)!!
            db.avatarDao().insert(Avatar(uid, file.absolutePath))
        } catch (e: IOException) {
            e.printStackTrace()
        }

        avatarMap[key] = avatar
    }
}
