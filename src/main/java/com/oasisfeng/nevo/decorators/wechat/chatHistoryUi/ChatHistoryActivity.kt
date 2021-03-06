package com.oasisfeng.nevo.decorators.wechat.chatHistoryUi

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
import android.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.oasisfeng.nevo.decorators.wechat.R
import com.oasisfeng.nevo.decorators.wechat.WeChatApp
import com.oasisfeng.nevo.decorators.wechat.chatHistoryUi.UserActivity.Companion.RESULT_REFRESH
import kotlinx.coroutines.*
import me.everything.android.ui.overscroll.OverScrollDecoratorHelper

class ChatHistoryActivity : Activity() {
    private lateinit var mDb: AppDatabase
    private lateinit var mChatSelectedSid: String
    private lateinit var mChatSelectedTitle: String
    private var mAdapterData = mutableListOf<Any>()
    private lateinit var mAdapter: ChatBubbleAdapter
    private lateinit var mRecycler: RecyclerView
    private lateinit var mLayout: LinearLayoutManager

    companion object {
        const val ACTION_NOTIFY_NEW_MESSAGE = "NOTIFY_NEW_MESSAGE"
        const val ACTION_NOTIFY_REFRESH_ALL = "NOTIFY_REFRESH_ALL"
        const val EXTRA_USER_ID = "user_id"
        const val EXTRA_USERNAME = "username"

        private const val STATE_USER = "user"
        private const val STATE_TITLE = "title"
        private const val STATE_RECYCLER = "recycler"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_history)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setActionBar(toolbar)

        mDb = (this.applicationContext as WeChatApp).db

        if (savedInstanceState != null) {
            val userId = savedInstanceState.getString(STATE_USER)
            val username = savedInstanceState.getString(STATE_TITLE)
            val recycler: Parcelable = savedInstanceState.getParcelable(STATE_RECYCLER)!!

            mChatSelectedSid = userId!!
            mChatSelectedTitle = username!!

            GlobalScope.launch(Dispatchers.Main) {
                refreshData()

                (mRecycler.layoutManager as LinearLayoutManager).onRestoreInstanceState(recycler)
            }
        } else {
            // entering activity from user list
            val userId = intent.getStringExtra(EXTRA_USER_ID)
            val username = intent.getStringExtra(EXTRA_USERNAME)

            if (userId != null && username != null) {
                mChatSelectedTitle = username
                mChatSelectedSid = userId

                GlobalScope.launch(Dispatchers.Main) {
                    refreshData()
                    mRecycler.scrollToPosition(mAdapter.itemCount-1)
                }
            }
        }

        val filter = IntentFilter()
        filter.addAction(ACTION_NOTIFY_NEW_MESSAGE)
        filter.addAction(ACTION_NOTIFY_REFRESH_ALL)
        registerReceiver(mBroadcastReceiver, filter)
    }

    private suspend fun refreshData() {
        val messages = mDb.messageDao().getAllBySidAsc(mChatSelectedSid)
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

        if (!this@ChatHistoryActivity::mAdapter.isInitialized) {
            mAdapter = ChatBubbleAdapter(this@ChatHistoryActivity, mAdapterData)
            mRecycler = findViewById(R.id.bubble_recycler)
            mRecycler.adapter = mAdapter
            mLayout = LinearLayoutManager(this@ChatHistoryActivity)
            mLayout.stackFromEnd = true
            mRecycler.layoutManager = mLayout
            OverScrollDecoratorHelper.setUpOverScroll(mRecycler, OverScrollDecoratorHelper.ORIENTATION_VERTICAL)
        }
    }

    override fun onBackPressed() {
        setResult(RESULT_REFRESH)
        finish()
        super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(STATE_USER, mChatSelectedSid)
        outState.putString(STATE_TITLE, mChatSelectedTitle)
        outState.putParcelable(STATE_RECYCLER, mRecycler.layoutManager?.onSaveInstanceState())

        super.onSaveInstanceState(outState)
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
                                setResult(RESULT_REFRESH)
                                finish()
                            }
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
                            setResult(RESULT_REFRESH)
                            finish()
                        }
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

                            val state = mRecycler.layoutManager?.onSaveInstanceState()
                            mAdapterData.add(bubble)
                            mAdapter.notifyItemInserted(mAdapter.itemCount)
                            val lastPos = mLayout.findLastCompletelyVisibleItemPosition()
                            // don't scroll to new item unless we're at the bottom
                            if (lastPos == mAdapter.itemCount-2) {
                                mRecycler.scrollToPosition(mAdapter.itemCount - 1)
                            } else {
                                mRecycler.layoutManager?.onRestoreInstanceState(state)
                            }
                        }

                        ACTION_NOTIFY_REFRESH_ALL -> {
                            GlobalScope.launch(Dispatchers.Main) {
                                mAdapterData.clear()
                                refreshData()
                                mAdapter.notifyDataSetChanged()
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(mBroadcastReceiver)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.chat_history_menu, menu)
        return true
    }
}