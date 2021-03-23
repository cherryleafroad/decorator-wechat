package com.oasisfeng.nevo.decorators.wechat.chatHistory

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.*
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.oasisfeng.nevo.decorators.wechat.WeChatDecorator
import com.oasisfeng.nevo.decorators.wechat.chatHistory.activity.ChatHistoryFragment
import com.oasisfeng.nevo.decorators.wechat.chatHistory.activity.ChatHistoryFragmentActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.lang.ref.WeakReference

class MessengerClient(val activity: ChatHistoryFragmentActivity) : ServiceConnection {
    private var mService: Messenger? = null
    private var bound = false
    private val mMessenger = Messenger(IncomingHandler(WeakReference(activity)))

    fun bindService(context: Context) {
        if (!bound) {
            Intent(context, MessengerService::class.java).also { intent ->
                context.bindService(intent, this, Context.BIND_AUTO_CREATE)
            }
        }
    }

    fun unbindService(context: Context) {
        if (bound) {
            sendServiceMsg(MessengerService.MSG_UNREGISTER_CLIENT)

            context.unbindService(this)
            bound = false
        }
    }

    fun sendServiceMsg(message: Int, data: Bundle? = null) {
        mService?.let { it ->
            try {
                val msg = Message.obtain(
                    null, message
                )

                data?.let { b ->
                    msg.data = b
                }

                msg.replyTo = mMessenger
                it.send(msg)
            } catch (e: RemoteException) {
                // nothing to do - it crahsed
            }
        }
    }

    fun notifyServiceOpenUid(uid: Long) {
        if (mService != null) {
            activity.lifecycleScope.launch(Dispatchers.IO) {
                val sid = activity.mSharedViewModel.getSidFromUid(uid)

                val b = Bundle()
                b.putString(ChatHistoryFragmentActivity.EXTRA_ID, sid)

                sendServiceMsg(MessengerService.MSG_SET_UI_CHAT_ID, b)
            }
        }
    }

    override fun onServiceConnected(
        className: ComponentName?,
        service: IBinder?
    ) {
        mService = Messenger(service)

        sendServiceMsg(MessengerService.MSG_REGISTER_CLIENT)
        sendServiceMsg(MessengerService.MSG_UI_OPEN)

        // As part of the sample, tell the user what happened.
        Log.d(WeChatDecorator.TAG, "Connected to ${className?.shortClassName}")
    }

    override fun onServiceDisconnected(className: ComponentName) {
        mService = null
        Log.d(WeChatDecorator.TAG, "Disconnected from ${className.shortClassName}")
    }

    internal class IncomingHandler(val activity: WeakReference<ChatHistoryFragmentActivity>) : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            val main = activity.get()

            if (main != null) {
                when (msg.what) {
                    MessengerService.MSG_NEW_REPLY_ARRAY -> {
                        msg.data.classLoader = ReplyIntent::class.java.classLoader
                        val data =
                            msg.data.getParcelableArrayList<ReplyIntent>(MessengerService.EXTRA_REPLY_ARRAY)!!

                        data.forEach { d ->
                            main.mSharedViewModel.replyIntents[d.uid] = d
                        }

                        if (main.currentFragment == ChatHistoryFragment.CHAT) {
                            // chat should also be updated as well
                            main.mSharedViewModel.apply {
                                replyIntents[main.chatFragment.mChatSelectedId].let {
                                    replyIntentEmitter.emit(it!!)
                                }
                            }
                        }
                    }

                    MessengerService.MSG_NEW_REPLY -> {
                        msg.data.classLoader = ReplyIntent::class.java.classLoader
                        val data =
                            msg.data.getParcelable<ReplyIntent>(MessengerService.EXTRA_REPLY)!!
                        main.mSharedViewModel.replyIntents[data.uid] = data
                        if (main.currentFragment == ChatHistoryFragment.CHAT) {
                            if (data.uid == main.chatFragment.mChatSelectedId) {
                                main.mSharedViewModel.replyIntentEmitter.emit(data)
                            }
                        }
                    }

                    else -> super.handleMessage(msg)
                }
            }
        }
    }
}