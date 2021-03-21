package com.oasisfeng.nevo.decorators.wechat.chatHistory


import android.annotation.SuppressLint
import android.app.RemoteInput
import android.app.Service
import android.content.Intent
import android.os.*
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import com.oasisfeng.nevo.decorators.wechat.WeChatApp
import kotlinx.parcelize.Parcelize


@Parcelize
data class ReplyIntent(
    val uid: Long,
    val reply_intent: Intent,
    val remote_input: RemoteInput
) : Parcelable

class MessengerService : Service(), LifecycleOwner {
    companion object {
        const val MSG_REGISTER_CLIENT = 1
        const val MSG_UNREGISTER_CLIENT = 2
        const val MSG_NEW_REPLY = 3
        const val MSG_NEW_REPLY_ARRAY = 4

        const val EXTRA_REPLY = "extra_reply"
        const val EXTRA_REPLY_ARRAY = "extra_reply_array"
    }

    private lateinit var lifecycleRegistry: LifecycleRegistry
    private val mMessenger = Messenger(IncomingHandler())

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry = LifecycleRegistry(this)
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        (applicationContext as WeChatApp).isMessengerServiceRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        return START_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        lifecycleRegistry.currentState = Lifecycle.State.RESUMED
        return mMessenger.binder
    }

    override fun onDestroy() {
        (applicationContext as WeChatApp).isMessengerServiceRunning = false
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        super.onDestroy()
    }

    var mClients = ArrayList<Messenger>()

    @SuppressLint("HandlerLeak")
    inner class IncomingHandler : Handler(Looper.myLooper()!!) {
        override fun handleMessage(msg: Message) {
            when (msg.what) {
                MSG_REGISTER_CLIENT -> {
                    (applicationContext as WeChatApp).isUiOpen = true
                    mClients.add(msg.replyTo)

                    // register events to be sent to this client - note - singleliveevent only works for ONE observation!
                    (applicationContext as WeChatApp).replyIntentEvent.observe(this@MessengerService, {
                        val newMsg = Message.obtain(
                            null, MSG_NEW_REPLY, 0, 0
                        )

                        val bundle = Bundle()
                        bundle.putParcelable(EXTRA_REPLY, it!!)
                        newMsg.data = bundle

                        mClients[0].send(newMsg)
                    })

                    val msgArr = Message.obtain(
                        null, MSG_NEW_REPLY_ARRAY, 0, 0
                    )

                    val bundleArr = Bundle()
                    val values = (applicationContext as WeChatApp).replyMap.values
                    if (values.isNotEmpty()) {
                        val replyArr = ArrayList(values)
                        bundleArr.putParcelableArrayList(EXTRA_REPLY_ARRAY, replyArr)
                        msgArr.data = bundleArr
                        msg.replyTo.send(msgArr)
                    }
                }

                MSG_UNREGISTER_CLIENT -> {
                    (applicationContext as WeChatApp).isUiOpen = false
                    (applicationContext as WeChatApp).replyIntentEvent.removeObservers(this@MessengerService)
                    mClients.remove(msg.replyTo)
                }

                else -> super.handleMessage(msg)
            }
        }
    }

    override fun getLifecycle(): Lifecycle {
        return lifecycleRegistry
    }
}
