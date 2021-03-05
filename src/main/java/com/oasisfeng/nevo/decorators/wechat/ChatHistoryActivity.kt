package com.oasisfeng.nevo.decorators.wechat

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_history)

        mDb = (this.applicationContext as WeChatApp).db

        GlobalScope.launch(Dispatchers.Main) {
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

                    val messages = mDb.messageDao().getAllBySidAsc(sid)
                    setTitle(title)

                    val bubbles = mutableListOf<Any>()

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

                        bubbles.add(bubble)
                    }

                    val adapter = ChatBubbleAdapter(this@ChatHistoryActivity, bubbles)
                    val recycler = findViewById<RecyclerView>(R.id.bubble_recycler)
                    recycler.adapter = adapter
                    val layout = LinearLayoutManager(this@ChatHistoryActivity)
                    
                    layout.stackFromEnd = true
                    recycler.layoutManager = layout

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