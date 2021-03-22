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


private enum class ChatHistoryFragment {
    USER_LIST,
    CHAT
}

class ChatHistoryFragmentActivity : AppCompatActivity() {
    private var mService: Messenger? = null
    private val mMessenger = Messenger(IncomingHandler(WeakReference(this)))
    private var bound = false
    // to access child fragments
    private val userListFragment = UserListFragment()
    private val chatFragment: ChatFragment
        get() = _chatFragment!!
    private var _chatFragment: ChatFragment? = null

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

                        if (main.currentFragment == ChatHistoryFragment.CHAT) {
                            // chat should also be updated as well
                            main.mSharedViewModel.apply {
                                replyIntents[main.chatFragment.mChatSelectedId].let {
                                    chatReplyIntent.postValue(it)
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
                                main.mSharedViewModel.chatReplyIntent.postValue(data)
                            }
                        }
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
                    null, MessengerService.MSG_REGISTER_CLIENT
                )
                msg.replyTo = mMessenger
                mService!!.send(msg)

                val msgOpen = Message.obtain(
                    null, MessengerService.MSG_UI_OPEN
                )
                mService!!.send(msgOpen)
            } catch (e: RemoteException) {
                // crashed
            }

            // As part of the sample, tell the user what happened.
            Log.d(TAG, "Connected to ${className?.shortClassName}")
        }

        override fun onServiceDisconnected(className: ComponentName) {
            mService = null
            Log.d(TAG, "Disconnected from ${className.shortClassName}")
        }
    }

    lateinit var mSharedViewModel: SharedViewModel
    private lateinit var mBinding: ActivityChatHistoryFragmentBinding
    private var currentFragment = ChatHistoryFragment.USER_LIST

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityChatHistoryFragmentBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mSharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        supportFragmentManager.addFragmentOnAttachListener { _, fragment -> run {
            currentFragment = when (fragment) {
                is ChatFragment -> {
                    ChatHistoryFragment.CHAT
                }

                is UserListFragment -> {
                    ChatHistoryFragment.USER_LIST
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

            // it can only have one observer, so make sure that's true
            mSharedViewModel.chatReplyIntent.removeObservers(this)
            _chatFragment = ChatFragment()
            supportFragmentManager.beginTransaction().apply {
                addToBackStack(null)
                add(R.id.fragment_frame, chatFragment)
                commit()
            }
        })
    }

    override fun onBackPressed() {
        when (currentFragment) {
            ChatHistoryFragment.CHAT -> {
                currentFragment = ChatHistoryFragment.USER_LIST

                chatFragment.mBinding.bubbleRecycler.isVerticalScrollBarEnabled = false
                // disable to prevent it from appearing on backpress
                userListFragment.mBinding.userRecycler.isVerticalScrollBarEnabled = false
                Handler(Looper.getMainLooper()).postDelayed({
                    userListFragment.mBinding.userRecycler.isVerticalScrollBarEnabled = true
                }, 500)

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

            ChatHistoryFragment.USER_LIST -> {
                finish()
            }
        }

        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()

        if (mService != null) {
            val msg = Message.obtain(
                null,
                MessengerService.MSG_UI_CLOSED
            )

            mService!!.send(msg)
        }
    }

    override fun onResume() {
        super.onResume()

        if (mService != null) {
            val msg = Message.obtain(
                null, MessengerService.MSG_UI_OPEN
            )

            mService!!.send(msg)

            // request an intent update since we weren't there to receive them
            val msg2 = Message.obtain(
                null, MessengerService.MSG_NEW_REPLY_ARRAY
            )
            msg2.replyTo = mMessenger
            mService!!.send(msg2)
        }

        if (currentFragment == ChatHistoryFragment.USER_LIST) {
            userListFragment.mBinding.userRecycler.isVerticalScrollBarEnabled = false
            Handler(Looper.getMainLooper()).postDelayed({
                userListFragment.mBinding.userRecycler.isVerticalScrollBarEnabled = true
            }, 500)
        }
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
        if (currentFragment == ChatHistoryFragment.CHAT) {
            if (event.action == MotionEvent.ACTION_DOWN) {
                val v = currentFocus
                if (v is EditText) {
                    val outRect = Rect()
                    val sendRect = Rect()
                    v.getGlobalVisibleRect(outRect)
                    chatFragment.mBinding.sendButton.getGlobalVisibleRect(sendRect)

                    if (!outRect.contains(event.rawX.toInt(), event.rawY.toInt()) &&
                        !sendRect.contains(event.rawX.toInt(), event.rawY.toInt())
                    ) {

                        // wechat doesn't clear focus, however I find it distracting that it won't go away
                        v.clearFocus()
                        val imm: InputMethodManager =
                            getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.hideSoftInputFromWindow(v.getWindowToken(), 0)
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}