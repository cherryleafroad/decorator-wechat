package com.oasisfeng.nevo.decorators.wechat.chatHistory.fragment

import android.app.AlertDialog
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.RemoteInput
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.transition.TransitionInflater
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.oasisfeng.nevo.decorators.wechat.R
import com.oasisfeng.nevo.decorators.wechat.WeChatDecorator.TAG
import com.oasisfeng.nevo.decorators.wechat.chatHistory.ReplyIntent
import com.oasisfeng.nevo.decorators.wechat.chatHistory.adapter.ChatBubbleAdapter
import com.oasisfeng.nevo.decorators.wechat.chatHistory.viewmodel.SharedViewModel
import com.oasisfeng.nevo.decorators.wechat.databinding.FragmentChatBinding
import kotlinx.coroutines.launch


class ChatFragment : Fragment() {
    private var _mBinding: FragmentChatBinding? = null
    val mBinding
        get() = _mBinding!!

    private lateinit var mAdapter: ChatBubbleAdapter
    private lateinit var mLayout: LinearLayoutManager

    private val mSharedModel: SharedViewModel by activityViewModels()

    private lateinit var mChatSelectedTitle: String
    private var mChatSelectedId: Long = 0

    private var replyIntent: ReplyIntent? = null

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

        if (mSharedModel.replyIntents.contains(data.uid)) {
            replyIntent = mSharedModel.replyIntents[data.uid]
            mBinding.inputIndicator.background = AppCompatResources.getDrawable(requireContext(), R.drawable.chat_input_indicator_enabled)
        }

        // register to update replyintent variable
        mSharedModel.chatReplyIntent.observe(this, {
            it ?: return@observe

            replyIntent = it
            mBinding.inputIndicator.background = AppCompatResources.getDrawable(requireContext(), R.drawable.chat_input_indicator_enabled)
        })

        mChatSelectedTitle = data.title
        mBinding.toolbarTitle.text = mChatSelectedTitle

        mBinding.toolbar.apply {
            inflateMenu(R.menu.chat_history_menu)
            setOnMenuItemClickListener {
                onOptionsItemSelected(it)
            }
        }

        mBinding.textInput.addTextChangedListener {
            mBinding.sendButton.isEnabled = mBinding.textInput.text.isNotEmpty() && replyIntent != null
        }

        // sending messages always scrolls to the end
        mBinding.sendButton.setOnClickListener {
            sendMessage()
            mBinding.bubbleRecycler.scrollToPosition(0)
        }

        // disable for first opening of chat
        mBinding.bubbleRecycler.isVerticalScrollBarEnabled = false
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
                mBinding.bubbleRecycler.isVerticalScrollBarEnabled = true
            }
        })

        var firstLoad = true
        mAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                if ((positionStart == 0 && firstLoad) || atEnd) {
                    mBinding.bubbleRecycler.scrollToPosition(0)

                    if (firstLoad) {
                        firstLoad = false
                    }
                }
            }
        })

        val handler = Handler(Looper.getMainLooper())
        // pretend that we are adjusting layout size
        mBinding.bubbleRecycler.addOnLayoutChangeListener { v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int ->
            if (atEnd) {
                mBinding.bubbleRecycler.scrollToPosition(0)
            }

            if (!firstLoad) {
                // hide the scrollbar until the layout is done
                mBinding.bubbleRecycler.isVerticalScrollBarEnabled = false

                // get rid of any running callbacks just in case there are some before this
                // that way there are no conflicts of interest
                handler.removeCallbacksAndMessages(null)
                // re-enable it cause we obviously want it -
                // last handler is the one that goes through
                handler.postDelayed({
                    mBinding.bubbleRecycler.isVerticalScrollBarEnabled = true
                }, 400)
            }
        }

        val pagedData = data.messageData
        pagedData.observe(viewLifecycleOwner, {
            it ?: return@observe

            lifecycleScope.launch {
                mAdapter.submitData(it)
            }
        })
    }

    override fun onStart() {
        super.onStart()

        val data = mSharedModel.chatData.value!!

        // add draft to textinput if one was saved
        if (mSharedModel.drafts.contains(data.uid)) {
            mBinding.textInput.setText(mSharedModel.drafts[data.uid], TextView.BufferType.EDITABLE)
        } else {
            mBinding.textInput.setText("", TextView.BufferType.EDITABLE)
        }
    }

    @Suppress("LocalVariableName")
    private fun sendMessage() {
        val ri = replyIntent?.remote_input!!
        val intent = replyIntent?.reply_intent!!

        val bundle = Bundle()
        bundle.putString(
            ri.resultKey, mBinding.textInput.text.toString()
        )

        val remote_inputs: Array<RemoteInput> = arrayOf(ri)
        RemoteInput.addResultsToIntent(remote_inputs, intent, bundle)

        mBinding.textInput.setText("",TextView.BufferType.EDITABLE)
        PendingIntent.getBroadcast(context, 0, intent.setPackage(context?.packageName), FLAG_UPDATE_CURRENT).send(context, 0, intent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.clear_chat -> {
                AlertDialog.Builder(requireContext())
                    .setTitle(getString(R.string.delete_chat))
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