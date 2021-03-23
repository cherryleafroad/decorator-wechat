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
import android.transition.TransitionInflater
import android.view.*
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_DRAGGING
import androidx.recyclerview.widget.RecyclerView.SCROLL_STATE_IDLE
import com.oasisfeng.nevo.decorators.wechat.R
import com.oasisfeng.nevo.decorators.wechat.chatHistory.ReplyIntent
import com.oasisfeng.nevo.decorators.wechat.chatHistory.adapter.ChatBubbleAdapter
import com.oasisfeng.nevo.decorators.wechat.chatHistory.viewmodel.SharedViewModel
import com.oasisfeng.nevo.decorators.wechat.databinding.FragmentChatBinding
import com.zhuinden.liveevent.observe
import kotlinx.coroutines.launch


class ChatFragment : Fragment() {
    private var _mBinding: FragmentChatBinding? = null
    val mBinding
        get() = _mBinding!!

    private lateinit var mAdapter: ChatBubbleAdapter
    private lateinit var mLayout: LinearLayoutManager

    private val mSharedModel: SharedViewModel by activityViewModels()

    private lateinit var mChatSelectedTitle: String
    var mChatSelectedId: Long = 0

    private var replyIntent: ReplyIntent? = null
    var restarted = false
    var inputText = ""

    companion object {
        const val STATE_RESTART = "state_restart"
        const val STATE_INPUT_TEXT = "state_input_text"

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

        if (savedInstanceState != null) {
            // used for when the activity was restarted
            mSharedModel.apply {
                restartedChat = savedInstanceState.getBoolean(STATE_RESTART)
                inputData = savedInstanceState.getString(STATE_INPUT_TEXT, "")
            }
        }

        if (mSharedModel.replyIntents.contains(data.uid)) {
            replyIntent = mSharedModel.replyIntents[data.uid]
            mBinding.inputIndicator.background = AppCompatResources.getDrawable(requireContext(), R.drawable.chat_input_indicator_enabled)
            mBinding.sendButton.isEnabled = mBinding.textInput.text.isNotEmpty()
        }

        // register to update replyintent variable
        mSharedModel.replyIntentEvent.observe(this, {
            replyIntent = it
            mBinding.inputIndicator.background = AppCompatResources.getDrawable(requireContext(), R.drawable.chat_input_indicator_enabled)
            mBinding.sendButton.isEnabled = mBinding.textInput.text.isNotEmpty()
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
            inputText = it.toString()
            mSharedModel.inputData = it.toString()
        }

        // we start out at the bottom
        var atEnd = true
        // sending messages always scrolls to the end
        mBinding.sendButton.setOnClickListener {
            atEnd = true
            sendMessage()
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

        // this is used to determine to ignore the layoutchangelistener
        var dragging = false

        mBinding.bubbleRecycler.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                when (newState) {
                    SCROLL_STATE_IDLE -> {
                        atEnd = !recyclerView.canScrollVertically(1)
                        dragging = false
                    }

                    SCROLL_STATE_DRAGGING -> {
                        dragging = true
                        atEnd = !recyclerView.canScrollVertically(1)
                        mBinding.bubbleRecycler.isVerticalScrollBarEnabled = true
                    }
                }
            }
        })

        var firstLoad = true
        mAdapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onItemRangeInserted(positionStart: Int, itemCount: Int) {
                super.onItemRangeInserted(positionStart, itemCount)

                if ((positionStart == 0 && atEnd) || firstLoad) {
                    mBinding.bubbleRecycler.scrollToPosition(0)

                    firstLoad = false
                }
            }
        })

        // pretend that we are adjusting layout size
        mBinding.bubbleRecycler.addOnLayoutChangeListener { _: View, _: Int, _: Int, _: Int, bottom: Int, _: Int, _: Int, _: Int, oldBottom: Int ->
            // same layout size triggers often, but there's no real change?
            if (oldBottom != bottom) {
                if (atEnd) {
                    mBinding.bubbleRecycler.scrollToPosition(0)
                }

                if (!dragging) {
                    // hide the scrollbar until the layout is done
                    mBinding.bubbleRecycler.isVerticalScrollBarEnabled = false
                }
            } else if (restarted) {
                mBinding.bubbleRecycler.scrollToPosition(0)
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

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putBoolean(STATE_RESTART, true)
        outState.putString(STATE_INPUT_TEXT, inputText)

        super.onSaveInstanceState(outState)
    }

    override fun onStart() {
        super.onStart()

        if (!restarted) {
            val data = mSharedModel.chatData.value!!

            // add draft to textinput if one was saved
            if (mSharedModel.inputData.isNotEmpty()) {
                mBinding.textInput.setText(mSharedModel.inputData, TextView.BufferType.EDITABLE)
            } else if (mSharedModel.drafts.contains(data.uid)) {
                mBinding.textInput.setText(mSharedModel.drafts[data.uid], TextView.BufferType.EDITABLE)
            } else {
                mBinding.textInput.setText("", TextView.BufferType.EDITABLE)
            }
        } else {
            restarted = false
            // restore text as it was last time
            mBinding.textInput.setText(inputText, TextView.BufferType.EDITABLE)
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