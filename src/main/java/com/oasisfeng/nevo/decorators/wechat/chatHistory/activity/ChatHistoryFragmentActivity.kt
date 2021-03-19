package com.oasisfeng.nevo.decorators.wechat.chatHistory.activity

import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.oasisfeng.nevo.decorators.wechat.R
import com.oasisfeng.nevo.decorators.wechat.chatHistory.viewmodel.SharedViewModel
import com.oasisfeng.nevo.decorators.wechat.chatHistory.fragment.ChatFragment
import com.oasisfeng.nevo.decorators.wechat.chatHistory.fragment.UserListFragment
import com.oasisfeng.nevo.decorators.wechat.databinding.ActivityChatHistoryFragmentBinding

class ChatHistoryFragmentActivity : AppCompatActivity() {
    private enum class Fragment {
        USER_LIST,
        CHAT
    }

    private lateinit var mSharedViewModel: SharedViewModel
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
        val userListFragment = UserListFragment()
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_frame, userListFragment)
            commit()
        }

        val chatFragment = ChatFragment()
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
                mSharedViewModel.clearChatData()
                mSharedViewModel.refreshUserList()
            }

            Fragment.USER_LIST -> {
                finish()
            }
        }

        super.onBackPressed()
    }
}