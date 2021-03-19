package com.oasisfeng.nevo.decorators.wechat.chatHistory.fragment

import android.app.AlertDialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.oasisfeng.nevo.decorators.wechat.R
import com.oasisfeng.nevo.decorators.wechat.chatHistory.viewmodel.SharedViewModel
import com.oasisfeng.nevo.decorators.wechat.chatHistory.adapter.ChatBubbleAdapter
import com.oasisfeng.nevo.decorators.wechat.databinding.FragmentChatBinding
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {
    private var _mBinding: FragmentChatBinding? = null
    private val mBinding
        get() = _mBinding!!

    private lateinit var mAdapter: ChatBubbleAdapter
    private lateinit var mLayout: LinearLayoutManager

    private val mSharedModel: SharedViewModel by activityViewModels()

    private lateinit var mChatSelectedTitle: String
    private var mChatSelectedId: Long = 0

    companion object {
        const val EXTRA_USERNAME = "username"
        const val ACTION_USERNAME_CHANGED = "username_changed"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val inflater = TransitionInflater.from(requireContext())
        enterTransition = inflater.inflateTransition(R.transition.slide_right)
        exitTransition = inflater.inflateTransition(R.transition.slide_left)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _mBinding = FragmentChatBinding.inflate(inflater, container, false)
        return mBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val data = mSharedModel.chatData.value!!
        mChatSelectedId = data.uid

        mChatSelectedTitle = data.title
        mBinding.toolbarTitle.text = mChatSelectedTitle

        mBinding.toolbar.apply {
            inflateMenu(R.menu.chat_history_menu)
            setOnMenuItemClickListener {
                onOptionsItemSelected(it)
            }
        }

        mAdapter = ChatBubbleAdapter(requireContext())
        mLayout = LinearLayoutManager(requireContext())
        mLayout.reverseLayout = true
        mLayout.stackFromEnd = true

        mBinding.bubbleRecycler.apply {
            adapter = mAdapter
            layoutManager = mLayout
        }

        // we start out at the bottom
        var atEnd = true
        mBinding.bubbleRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                val atBottom = recyclerView.canScrollVertically(-1) && !recyclerView.canScrollVertically(1)
                atEnd = atBottom && newState == RecyclerView.SCROLL_STATE_IDLE
            }
        })

        var firstLoad = true
        mAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                if ((positionStart == 0 && firstLoad) || atEnd) {
                    mBinding.bubbleRecycler.scrollToPosition(0)
                    firstLoad = false
                }
            }
        })

        val pagedData = data.messageData
        pagedData.observe(viewLifecycleOwner, {
            it ?: return@observe

            lifecycleScope.launch {
                mAdapter.submitData(it)
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear_chat -> {
                    AlertDialog.Builder(requireContext())
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
                            mSharedModel.deleteUser(mChatSelectedId)
                            parentFragmentManager.popBackStack()
                        }

                        .setNegativeButton(android.R.string.cancel, null)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private val mUsernameChanged: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            mChatSelectedTitle = intent.getStringExtra(EXTRA_USERNAME)!!
            mBinding.toolbarTitle.text = mChatSelectedTitle
        }
    }

    override fun onResume() {
        super.onResume()

        val filter = IntentFilter()
        filter.addAction(ACTION_USERNAME_CHANGED)
        requireActivity().registerReceiver(mUsernameChanged, filter)
    }

    override fun onPause() {
        super.onPause()
        requireActivity().unregisterReceiver(mUsernameChanged)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _mBinding = null
    }
}