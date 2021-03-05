package com.oasisfeng.nevo.decorators.wechat.chatui

import android.content.Context
import android.content.Intent
import com.oasisfeng.nevo.decorators.wechat.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object DatabaseHelpers {
    lateinit var mDb: AppDatabase

    @JvmStatic
    fun addReply(context: Context, id: String, reply: String) {
        if (!this::mDb.isInitialized) {
            mDb = ((context.applicationContext as WeChatApp)).db
        }

        GlobalScope.launch(Dispatchers.Main) {
            addReplyInternal(context, id, reply)
        }
    }

    private suspend fun addReplyInternal(context: Context, id: String, reply: String) {
        val message = Message(id, reply, true)
        mDb.messageDao().insert(message)

        // notify of any new messages if you're in the activity
        val intent = Intent(ChatHistoryActivity.ACTION_NOTIFY_NEW_MESSAGE)
        intent.putExtra(ChatHistoryActivity.EXTRA_USER_ID, id)
        intent.setPackage(context.packageName)
        context.sendBroadcast(intent)
    }
}