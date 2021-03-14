package com.oasisfeng.nevo.decorators.wechat.chatHistoryUi

import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toolbar
import androidx.lifecycle.*
import androidx.paging.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.oasisfeng.nevo.decorators.wechat.R
import com.oasisfeng.nevo.decorators.wechat.WeChatApp
import com.oasisfeng.nevo.decorators.wechat.chatHistoryUi.UserActivity.Companion.RESULT_REFRESH
import kotlinx.coroutines.*


class ChatHistoryActivity : Activity(), LifecycleOwner {
    private lateinit var mDb: AppDatabase
    private var mChatSelectedId: Long = -1
    private lateinit var mChatSelectedTitle: String
    private lateinit var mAdapter: ChatBubbleAdapter
    private var mLifecycleRegistry = LifecycleRegistry(this)
    private lateinit var mRecycler: RecyclerView
    private lateinit var mLayout: LinearLayoutManager
    private var mThemeChangeBypass = false

    init {
        mLifecycleRegistry.currentState = Lifecycle.State.INITIALIZED
    }

    companion object {
        const val EXTRA_USER_ID = "user_id"
        const val EXTRA_USERNAME = "username"
        const val ACTION_USERNAME_CHANGED = "username_changed"

        private const val STATE_USER_ID = "user"
        private const val STATE_TITLE = "title"
        private const val STATE_CHANGED_THEME = "changed_theme"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_history)

        mDb = (this.applicationContext as WeChatApp).db

        mLifecycleRegistry = LifecycleRegistry(this)
        mLifecycleRegistry.currentState = Lifecycle.State.CREATED

        mAdapter = ChatBubbleAdapter(this@ChatHistoryActivity)
        mRecycler = findViewById(R.id.bubble_recycler)
        mRecycler.adapter = mAdapter
        mLayout = LinearLayoutManager(this@ChatHistoryActivity)
        mLayout.reverseLayout = true
        mRecycler.layoutManager = mLayout

        val userId: Long
        val username: String
        if (savedInstanceState != null) {
            userId = savedInstanceState.getLong(STATE_USER_ID)
            username = savedInstanceState.getString(STATE_TITLE)!!
            mThemeChangeBypass = savedInstanceState.getBoolean(STATE_CHANGED_THEME)
        } else {
            // entering activity from user list
            userId = intent.getLongExtra(EXTRA_USER_ID, -1)
            username = intent.getStringExtra(EXTRA_USERNAME)!!
        }

        mChatSelectedTitle = username
        mChatSelectedId = userId

        val filter = IntentFilter()
        filter.addAction(ACTION_USERNAME_CHANGED)
        registerReceiver(mUsernameChanged, filter)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setActionBar(toolbar)
        actionBar?.title = mChatSelectedTitle

        val items = Pager(
            PagingConfig(
                initialLoadSize = 80,
                pageSize = 40,
                prefetchDistance = 50,
                enablePlaceholders = false,
                maxSize = 200
            )
        ) {
            mDb.messageDao().getMessagesWithAvatarPaged(mChatSelectedId)
        }.liveData

        // TODO: some kinda of livedata onfinished callback to load at bottom when activity finished
        // TODO: and if user is at the bottom, move to the new message in-line

        // check if we can scroll to bottom
        /*var firstLoad = true
        lifecycleScope.launch {
            if (firstLoad) {
                Log.d("Nevo.Decorator[WeChat]", "Can scroll? " + mRecycler.canScrollVertically(-1))

                mRecycler.scrollToPosition(mAdapter.itemCount)
                firstLoad = false
            }
        }*/

        // now setup actual perpetual live data listener
        items.observe(this@ChatHistoryActivity, Observer {
            it ?: return@Observer

            lifecycleScope.launch {
                mAdapter.submitData(it)
            }
        })
    }

    override fun onStart() {
        super.onStart()
        mLifecycleRegistry.currentState = Lifecycle.State.STARTED
    }

    override fun onResume() {
        super.onResume()
        mLifecycleRegistry.currentState = Lifecycle.State.RESUMED
    }

    override fun onDestroy() {
        super.onDestroy()
        mLifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        unregisterReceiver(mUsernameChanged)
    }

    private val mUsernameChanged: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mChatSelectedTitle = intent.getStringExtra(EXTRA_USERNAME)!!
            actionBar?.title = mChatSelectedTitle
        }
    }

    override fun onBackPressed() {
        if (!mThemeChangeBypass) {
            setResult(RESULT_REFRESH)
        }
        finish()
        super.onBackPressed()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putLong(STATE_USER_ID, mChatSelectedId)
        outState.putString(STATE_TITLE, mChatSelectedTitle)
        outState.putBoolean(STATE_CHANGED_THEME, mThemeChangeBypass)

        super.onSaveInstanceState(outState)
    }

    // detect system theme change
    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)

        // detect sysetm theme change in order to work around a bug which calls onCreate and
        // setActivityResult both in userlist, causing double list
        mThemeChangeBypass = true
        //AppCompatDelegate.setDefaultNightMode(MODE_NIGHT_FOLLOW_SYSTEM)
        this.recreate()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear_chat -> {
                if (mChatSelectedId >= 0) {
                    AlertDialog.Builder(this)
                        .setTitle(getString(R.string.delete_chat).replace("%s", mChatSelectedTitle))
                        .setMessage(
                            getString(R.string.delete_chat_summary).replace(
                                "%s",
                                mChatSelectedTitle
                            )
                        )

                        .setPositiveButton(
                            android.R.string.ok
                        ) { _, _ ->
                            GlobalScope.launch(Dispatchers.IO) {
                                mDb.userDao().deleteByUid(mChatSelectedId)
                                if (!mThemeChangeBypass) {
                                    setResult(RESULT_REFRESH)
                                }
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
                        GlobalScope.launch(Dispatchers.IO) {
                            mDb.userDao().deleteAll()
                            if (!mThemeChangeBypass) {
                                setResult(RESULT_REFRESH)
                            }
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

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.chat_history_menu, menu)
        return true
    }

    override fun getLifecycle(): Lifecycle {
        return mLifecycleRegistry
    }

    fun <T : Any> T?.notNull(f: (it: T) -> Unit) {
        if (this != null) f(this)
    }
}