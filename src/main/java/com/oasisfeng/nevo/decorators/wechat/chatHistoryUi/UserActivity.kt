package com.oasisfeng.nevo.decorators.wechat.chatHistoryUi

import android.app.Activity
import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.oasisfeng.nevo.decorators.wechat.R
import com.oasisfeng.nevo.decorators.wechat.WeChatApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class UserActivity : Activity() {
    private lateinit var mDb: AppDatabase
    private var mAdapterData = mutableListOf<UserItem>()
    private lateinit var mAdapter: UserAdapter
    private lateinit var mRecycler: RecyclerView

    companion object {
        const val ACTION_NOTIFY_USER_CHANGE = "NOTIFY_USER_CHANGE"
        const val RESULT_REFRESH = 3
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user)

        mDb = (this.applicationContext as WeChatApp).db
        mAdapter = UserAdapter(this, mAdapterData)
        mRecycler = findViewById(R.id.user_recycler)
        mRecycler.adapter = mAdapter
        val layout = LinearLayoutManager(this)
        mRecycler.layoutManager = layout

        val filter = IntentFilter()
        filter.addAction(ACTION_NOTIFY_USER_CHANGE)
        registerReceiver(mBroadcastReceiver, filter)

        loadUsers()
    }

    private fun loadUsers() {
        mAdapterData.clear()
        GlobalScope.launch(Dispatchers.Main) {
            val users = mDb.userDao().getAll()

            for (user in users) {
                mAdapterData.add(
                    UserItem(
                        user!!.sid,
                        user.username
                    )
                )
            }

            mAdapter.notifyDataSetChanged()
        }
    }

    fun userOnClick(userId: String, username: String) {
        val intent = Intent(this, ChatHistoryActivity::class.java).apply {
            putExtra(ChatHistoryActivity.EXTRA_USER_ID, userId)
            putExtra(ChatHistoryActivity.EXTRA_USERNAME, username)
        }

        startActivityForResult(intent, RESULT_REFRESH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (resultCode) {
            RESULT_REFRESH -> {
                // we require a user refresh if user deleted a chat
                loadUsers()
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
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
                            mAdapterData.clear()
                            mAdapter.notifyDataSetChanged()
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

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        menu?.findItem(R.id.clear_chat)?.isEnabled = false

        return super.onPrepareOptionsMenu(menu)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.chat_history_menu, menu)
        return true
    }

    override fun onBackPressed() {
        finish()
        super.onBackPressed()
    }

    private val mBroadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            mAdapterData.clear()
            GlobalScope.launch(Dispatchers.Main) {
                this@UserActivity.loadUsers()
            }

        }
    }

    override fun onDestroy() {
        super.onDestroy()

        unregisterReceiver(mBroadcastReceiver)
    }
}