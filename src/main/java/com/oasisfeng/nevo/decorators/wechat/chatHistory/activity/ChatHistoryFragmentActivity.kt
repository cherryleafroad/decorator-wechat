package com.oasisfeng.nevo.decorators.wechat.chatHistory.activity

import android.graphics.Rect
import android.os.*
import android.view.MotionEvent
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.oasisfeng.nevo.decorators.wechat.R
import com.oasisfeng.nevo.decorators.wechat.chatHistory.MessengerClient
import com.oasisfeng.nevo.decorators.wechat.chatHistory.MessengerService
import com.oasisfeng.nevo.decorators.wechat.chatHistory.fragment.ChatFragment
import com.oasisfeng.nevo.decorators.wechat.chatHistory.fragment.SettingsFragment
import com.oasisfeng.nevo.decorators.wechat.chatHistory.fragment.UserListFragment
import com.oasisfeng.nevo.decorators.wechat.chatHistory.viewmodel.SharedViewModel
import com.oasisfeng.nevo.decorators.wechat.databinding.ActivityChatHistoryFragmentBinding


enum class ChatHistoryFragment {
    USER_LIST,
    CHAT,
    SETTINGS
}

class ChatHistoryFragmentActivity : AppCompatActivity() {
    companion object {
        const val EXTRA_ID = "chat_sid"
    }

    private val messengerClient = MessengerClient(this)
    // to access child fragments
    private val userListFragment = UserListFragment()
    val chatFragment: ChatFragment
        get() = _chatFragment!!
    private var _chatFragment: ChatFragment? = null

    lateinit var mSharedViewModel: SharedViewModel
    private lateinit var mBinding: ActivityChatHistoryFragmentBinding
    var currentFragment = ChatHistoryFragment.USER_LIST


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = ActivityChatHistoryFragmentBinding.inflate(layoutInflater)
        setContentView(mBinding.root)

        mSharedViewModel = ViewModelProvider(this).get(SharedViewModel::class.java)

        supportFragmentManager.addFragmentOnAttachListener { _, fragment ->
            currentFragment = when (fragment) {
                is ChatFragment -> {
                    ChatHistoryFragment.CHAT
                }

                is UserListFragment -> {
                    ChatHistoryFragment.USER_LIST
                }

                is SettingsFragment -> {
                    ChatHistoryFragment.SETTINGS
                }

                else -> throw NotImplementedError()
            }
        }

        // start up userlist
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_frame, userListFragment)
            commit()
        }

        mSharedViewModel.chatData.observe(this, {
            it ?: return@observe

            _chatFragment = ChatFragment()

            // chat got restarted, due to theme change, so place the text back to normal
            if (mSharedViewModel.restartedChat) {
                mSharedViewModel.restartedChat = false
                chatFragment.restarted = true
                chatFragment.inputText = mSharedViewModel.inputData
            }

            messengerClient.notifyServiceOpenUid(it.uid)
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

                // just in case it got set to true because of entering/exiting chat through onpause/onresume
                mSharedViewModel.restartedChat = false
                mSharedViewModel.inputData = ""

                messengerClient.sendServiceMsg(MessengerService.MSG_DEL_UI_CHAT_ID)

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

            ChatHistoryFragment.SETTINGS -> {
                currentFragment = ChatHistoryFragment.USER_LIST
            }

            ChatHistoryFragment.USER_LIST -> {
                finish()
            }
        }

        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()

        messengerClient.sendServiceMsg(MessengerService.MSG_UI_CLOSED)
    }

    override fun onResume() {
        super.onResume()

        messengerClient.sendServiceMsg(MessengerService.MSG_UI_OPEN)
        if (currentFragment == ChatHistoryFragment.CHAT) {
            messengerClient.notifyServiceOpenUid(chatFragment.mChatSelectedId)
        }
        // request an intent update since we weren't there to receive them
        messengerClient.sendServiceMsg(MessengerService.MSG_NEW_REPLY_ARRAY)

        if (currentFragment == ChatHistoryFragment.CHAT) {
            chatFragment.mBinding.bubbleRecycler.isVerticalScrollBarEnabled = false
        }
    }

    override fun onStart() {
        super.onStart()
        messengerClient.bindService(this)
    }

    override fun onStop() {
        super.onStop()
        messengerClient.unbindService(this)
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
                        imm.hideSoftInputFromWindow(v.windowToken, 0)
                    }
                }
            }
        }
        return super.dispatchTouchEvent(event)
    }
}