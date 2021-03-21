package com.oasisfeng.nevo.decorators.wechat.chatHistory.activity

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Rect
import android.os.*
import android.util.Log
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.oasisfeng.nevo.decorators.wechat.R
import com.oasisfeng.nevo.decorators.wechat.WeChatDecorator.TAG
import com.oasisfeng.nevo.decorators.wechat.chatHistory.MessengerService
import com.oasisfeng.nevo.decorators.wechat.chatHistory.ReplyIntent
import com.oasisfeng.nevo.decorators.wechat.chatHistory.fragment.ChatFragment
import com.oasisfeng.nevo.decorators.wechat.chatHistory.fragment.UserListFragment
import com.oasisfeng.nevo.decorators.wechat.chatHistory.viewmodel.SharedViewModel
import com.oasisfeng.nevo.decorators.wechat.databinding.ActivityChatHistoryFragmentBinding
import java.lang.ref.WeakReference


private enum class Fragment {
    USER_LIST,
    CHAT
}

class ChatHistoryFragmentActivity : AppCompatActivity() {
    private var mService: Messenger? = null
    private val mMessenger = Messenger(IncomingHandler(WeakReference(this)))
    private var bound = false
    // to access child fragments
    private val userListFragment = UserListFragment()
    private val chatFragment = ChatFragment()

    internal class IncomingHandler(val activity: WeakReference<ChatHistoryFragmentActivity>) : Handler(
        Looper.myLooper()!!
    ) {
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
                    }

                    MessengerService.MSG_NEW_REPLY -> {
                        msg.data.classLoader = ReplyIntent::class.java.classLoader
                        val data =
                            msg.data.getParcelable<ReplyIntent>(MessengerService.EXTRA_REPLY)!!
                        main.mSharedViewModel.replyIntents[data.uid] = data
                        main.mSharedViewModel.chatReplyIntent.postValue(data)
                    }

                    else -> super.handleMessage(msg)
                }
            }
        }
    }

    private val mConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(
            className: ComponentName?,
            service: IBinder?
        ) {
            mService = Messenger(service)

            try {
                val msg = Message.obtain(
                    null,
                    MessengerService.MSG_REGISTER_CLIENT
                )
                msg.replyTo = mMessenger
                mService!!.send(msg)
            } catch (e: RemoteException) {
                // crashed
            }

            // As part of the sample, tell the user what happened.
            Log.d(TAG, "Connected to ${className?.className}")
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mService = null
            Log.d(TAG, "Disconnected from ${className.className}")
        }
    }

    lateinit var mSharedViewModel: SharedViewModel
    private lateinit var mBinding: ActivityChatHistoryFragmentBinding
    private var currentFragment = Fragment.USER_LIST

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityChatHistoryFragmentBinding.inflate(layoutInflater)
        setContentView(mBinding.root)



        mSharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        supportFragmentManager.addFragmentOnAttachListener { _, fragment -> run {
            currentFragment = when (fragment) {
                is ChatFragment -> {
                    Fragment.CHAT
                }

                is UserListFragment -> {
                    Fragment.USER_LIST
                }

                else -> throw NotImplementedError()
            }
        } }

        // start up userlist
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_frame, userListFragment)
            commit()
        }

        mSharedViewModel.chatData.observe(this, {
            it ?: return@observe

            supportFragmentManager.beginTransaction().apply {
                addToBackStack(null)
                add(R.id.fragment_frame, chatFragment)
                commit()
            }
        })
    }

    override fun onBackPressed() {
        when (currentFragment) {
            Fragment.CHAT -> {
                currentFragment = Fragment.USER_LIST

                chatFragment.mBinding.bubbleRecycler.isVerticalScrollBarEnabled = false

                // check for drafts and if so, save it
                val uid = mSharedViewModel.chatData.value?.uid!!
                val draft = chatFragment.mBinding.textInput.text.toString()
                if (draft.isNotEmpty()) {
                    mSharedViewModel.drafts[uid] = draft
                    mSharedViewModel.saveDraft(uid, draft)
                } else {
                    mSharedViewModel.drafts.remove(uid)
                    mSharedViewModel.deleteDraft(uid)
                }

                mSharedViewModel.clearChatData()
                mSharedViewModel.refreshUserList()
            }

            Fragment.USER_LIST -> {
                finish()
            }
        }

        super.onBackPressed()
    }

    override fun onStart() {
        super.onStart()

        Intent(this, MessengerService::class.java).also { intent ->
            bindService(intent, mConnection, Context.BIND_AUTO_CREATE)
        }
    }

    override fun onStop() {
        super.onStop()

        if (bound) {
            if (mService != null) {
                try {
                    val msg = Message.obtain(
                        null,
                        MessengerService.MSG_UNREGISTER_CLIENT
                    )
                    msg.replyTo = mMessenger
                    mService!!.send(msg)
                } catch (e: RemoteException) {
                    // nothing to do - it crahsed
                }
            }

            unbindService(mConnection)
            bound = false
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent): Boolean {
        if (event.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                val outRect = Rect()
                val sendRect = Rect()
                v.getGlobalVisibleRect(outRect)
                chatFragment.mBinding.sendButton.getGlobalVisibleRect(sendRect)

                if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt()) &&
                    !sendRect.contains(event.rawX.toInt(), event.rawY.toInt())) {

                    //v.clearFocus()
                    val imm: InputMethodManager =
                        getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }


}