package com.oasisfeng.nevo.decorators.wechat

import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Parcelable
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.oasisfeng.nevo.decorators.wechat.chatui.ChatBubbleAdapter
import com.oasisfeng.nevo.decorators.wechat.chatui.ChatBubbleReceiver
import com.oasisfeng.nevo.decorators.wechat.chatui.ChatBubbleSender
import kotlinx.coroutines.*

class ChatHistoryActivity : Activity() {
    private lateinit var mDb: AppDatabase
    private lateinit var mChatSelectedSid: String
    private lateinit var mChatSelectedTitle: String
    private var mAdapterData = mutableListOf<Any>()
    private lateinit var mAdapter: ChatBubbleAdapter
    private lateinit var mRecycler: RecyclerView

    companion object {
        @JvmField
        val ACTION_NOTIFY_NEW_MESSAGE = "NOTIFY_NEW_MESSAGE"
        @JvmField
        val ACTION_NOTIFY_REFRESH_ALL = "NOTIFY_REFRESH_ALL"
        @JvmField
        val ACTION_NOTIFY_USER_CHANGE = "NOTIFY_USER_CHANGE"
        @JvmField
        val EXTRA_USER_ID = "user_id"

        private const val STATE_USER = "user"
        private const val STATE_TITLE = "title"
        private const val STATE_RECYCLER = "recycler"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_history)

        mDb = (this.applicationContext as WeChatApp).db

        updateButtons()

        mAdapter = ChatBubbleAdapter(this, mAdapterData)
        mRecycler = findViewById<RecyclerView>(R.id.bubble_recycler)
        mRecycler.adapter = mAdapter
        val layout = LinearLayoutManager(this)
        layout.reverseLayout = true
        mRecycler.layoutManager = layout

        val filter = IntentFilter()
        filter.addAction(ACTION_NOTIFY_NEW_MESSAGE)
        filter.addAction(ACTION_NOTIFY_REFRESH_ALL)
        filter.addAction((ACTION_NOTIFY_USER_CHANGE))
        registerReceiver(mBroadcastReceiver, filter)

        if (savedInstanceState != null) {
            val user = savedInstanceState.getString(STATE_USER, "")
            val title = savedInstanceState.getString(STATE_TITLE, "")
            val recycler: Parcelable = savedInstanceState.getParcelable(STATE_RECYCLER)!!

            // we were in a chat before! oh!
            if (user.isNotEmpty() && title.isNotEmpty()) {
                mChatSelectedSid = user
                mChatSelectedTitle = title

                setTitle(mChatSelectedTitle)

                GlobalScope.launch(Dispatchers.Main) {
                    refreshMessageView(user)
                    (mRecycler.layoutManager as LinearLayoutManager).onRestoreInstanceState(recycler)
                }
            }
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        if (this::mChatSelectedSid.isInitialized && this::mChatSelectedTitle.isInitialized) {
            outState.putString(STATE_USER, mChatSelectedSid)
            outState.putString(STATE_TITLE, mChatSelectedTitle)
            outState.putParcelable(STATE_RECYCLER, mRecycler.layoutManager?.onSaveInstanceState())
        }

        super.onSaveInstanceState(outState)
    }

    private fun updateButtons() {
        GlobalScope.launch(Dispatchers.Main) {
            val buttonContainer = this@ChatHistoryActivity.findViewById<LinearLayout>(R.id.buttonContainer)
            // remove all buttons to refresh it
            buttonContainer.removeAllViews()

            val users = mDb.userDao().getAll()
            if (users.isNotEmpty()) {
                if (users.isNotEmpty()) {
                    updateButtons(users)
                }
            }
        }
    }

    private fun setTitle(title: String) {
        val titleWidget = this.findViewById<TextView>(R.id.title)
        titleWidget.setText(title, TextView.BufferType.NORMAL)
    }

    private fun updateButtons(users: List<User?>) {
        val buttonContainer = this.findViewById<LinearLayout>(R.id.buttonContainer)
        for ((i, user) in users.withIndex()) {
            if (this::mChatSelectedTitle.isInitialized && mChatSelectedTitle.isNotEmpty() &&
                this::mChatSelectedSid.isInitialized && mChatSelectedSid.isNotEmpty()) {

                if (mChatSelectedSid == user?.sid && user.username != mChatSelectedTitle) {
                    setTitle(user.username)
                }
            }

            val btnTag = Button(this)
            btnTag.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            btnTag.text = user!!.username
            btnTag.id = i
            btnTag.setTag(R.string.tag_sid, user.sid)
            btnTag.setTag(R.string.tag_title, user.username)

            btnTag.setOnClickListener {
                GlobalScope.launch(Dispatchers.Main) {
                    val sid = it.getTag(R.string.tag_sid) as String
                    val title = it.getTag(R.string.tag_title) as String

                    setTitle(title)

                    refreshMessageView(sid)

                    mChatSelectedSid = sid
                    mChatSelectedTitle = title
                }
            }

            buttonContainer.addView(btnTag)
        }
    }

    private fun resetScreen(deletedSingleChat: Boolean = true) {
        val buttonContainer = this.findViewById<LinearLayout>(R.id.buttonContainer)
        //val chatbox = this.findViewById<TextView>(R.id.textView2)
        val titleWidget = this.findViewById<TextView>(R.id.title)

        if (!deletedSingleChat) {
            buttonContainer.removeAllViews()
        } else {
            for (b in buttonContainer.children) {
                val sid = b.getTag(R.string.tag_sid)
                if (sid == mChatSelectedSid) {
                    buttonContainer.removeView(b)
                    break
                }
            }
        }

        titleWidget.text = ""
        mAdapterData.clear()
        mAdapter.notifyDataSetChanged()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear_chat -> {
                if (this::mChatSelectedSid.isInitialized && mChatSelectedSid.isNotEmpty()) {
                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.delete_chat).replace("%s", mChatSelectedTitle))
                        .setMessage(getString(R.string.delete_chat_summary).replace("%s", mChatSelectedTitle))

                        .setPositiveButton(
                            android.R.string.ok
                        ) { _, _ ->
                            GlobalScope.launch(Dispatchers.Main) {
                                mDb.userDao().deleteUserBySid(mChatSelectedSid)
                                mDb.messageDao().deleteAllBySid(mChatSelectedSid)
                                mChatSelectedSid = ""
                                mChatSelectedTitle = ""
                                invalidateOptionsMenu()
                            }

                            resetScreen()
                        }

                        .setNegativeButton(android.R.string.no, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
                }
                true
            }
            R.id.clear_all -> {
                AlertDialog.Builder(this)
                    .setTitle(getString(R.string.delete_chat_all))
                    .setMessage(getString(R.string.delete_chat_all_summary))

                    .setPositiveButton(
                        android.R.string.ok
                    ) { _, _ ->
                        GlobalScope.launch(Dispatchers.Main) {
                            mDb.userDao().deleteAll()
                            mDb.messageDao().deleteAll()
                            mChatSelectedSid = ""
                            mChatSelectedTitle = ""
                            invalidateOptionsMenu()
                        }

                        resetScreen(false)
                    }

                    .setNegativeButton(android.R.string.no, null)
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val userId = intent?.getStringExtra(EXTRA_USER_ID)

            when (intent?.action) {
                ACTION_NOTIFY_USER_CHANGE -> {
                    updateButtons()
                    return
                }
            }

            if (this@ChatHistoryActivity::mChatSelectedSid.isInitialized && userId == mChatSelectedSid) {
                GlobalScope.launch(Dispatchers.Main) {

                    when (intent.action) {
                        ACTION_NOTIFY_NEW_MESSAGE -> {
                            val message = mDb.messageDao().getLatestMessagesByUserLimit(userId, 1)

                            val isReply = message[0]?.is_reply!!
                            val bubble: Any = if (!isReply) {
                                ChatBubbleReceiver(
                                    message[0]?.message!!
                                )
                            } else {
                                ChatBubbleSender(
                                    message[0]?.message!!
                                )
                            }

                            mAdapterData.add(0, bubble)
                            mAdapter.notifyDataSetChanged()
                        }

                        ACTION_NOTIFY_REFRESH_ALL -> {
                            refreshMessageView(userId)
                        }
                    }
                }
            }
        }
    }

    private suspend fun refreshMessageView(user_sid: String) {
        mAdapterData.clear()

        val messages = mDb.messageDao().getAllBySidDesc(user_sid)
        for (message in messages) {
            val bubble: Any = if (!message!!.is_reply) {
                ChatBubbleReceiver(
                    message.message
                )
            } else {
                ChatBubbleSender(
                    message.message
                )
            }

            mAdapterData.add(bubble)
        }

        mAdapter.notifyDataSetChanged()
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBroadcastReceiver)
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.clear_chat)?.isEnabled = this::mChatSelectedSid.isInitialized && mChatSelectedSid.isNotEmpty()

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.chat_history_menu, menu)
        return true
    }
}