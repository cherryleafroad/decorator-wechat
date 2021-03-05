package com.oasisfeng.nevo.decorators.wechat.chatui

import android.content.Context
import com.oasisfeng.nevo.decorators.wechat.AppDatabase
import com.oasisfeng.nevo.decorators.wechat.Message
import com.oasisfeng.nevo.decorators.wechat.WeChatApp
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
            addReplyInternal(id, reply)
        }
    }

    private suspend fun addReplyInternal(id: String, reply: String) {
        val message = Message(id, reply, true)
        mDb.messageDao().insert(message)
    }
}