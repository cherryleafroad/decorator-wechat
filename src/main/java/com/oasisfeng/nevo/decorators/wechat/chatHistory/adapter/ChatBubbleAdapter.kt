package com.oasisfeng.nevo.decorators.wechat.chatHistory.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.ArrayMap
import android.view.LayoutInflater
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.oasisfeng.nevo.decorators.wechat.R
import com.oasisfeng.nevo.decorators.wechat.chatHistory.DateConverter
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.MessageWithAvatar
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.type.ChatType
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.type.MessageType
import com.oasisfeng.nevo.decorators.wechat.chatHistory.fragment.ChatFragment
import com.oasisfeng.nevo.decorators.wechat.chatHistory.viewmodel.SelfUserData
import com.oasisfeng.nevo.decorators.wechat.databinding.*


class ChatBubbleAdapter(
    private val context: Context,
    private val activity: ChatFragment,
    private val selfUserData: SelfUserData
) : PagingDataAdapter<MessageWithAvatar, ChatBubbleAdapter.ChatBubbleViewHolder<MessageWithAvatar>>(
    DIFF_CALLBACK
) {

    private lateinit var avatar: Drawable
    private lateinit var groupAvatars: ArrayMap<String, Drawable>

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MessageWithAvatar>() {
            override fun areContentsTheSame(oldItem: MessageWithAvatar, newItem: MessageWithAvatar): Boolean {
                return oldItem.message.message == newItem.message.message
            }

            override fun areItemsTheSame(oldItem: MessageWithAvatar, newItem: MessageWithAvatar): Boolean {
                return oldItem.message == newItem.message
            }
        }
    }

    abstract class ChatBubbleViewHolder<MessageWithAvatar>(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(item: MessageWithAvatar, position: Int)
    }

    inner class ChatBubbleReceiverViewHolder(private val binding: ItemChatBubbleReceiverBinding) : ChatBubbleViewHolder<MessageWithAvatar>(binding) {
        override fun bind(item: MessageWithAvatar, position: Int) {
            binding.chatBubbleReceiver.text = item.message.message

            val avFilename = item.avatar.filename
            if (item.message.chat_type == ChatType.CHAT) {
                if (avFilename.isNotEmpty()) {
                    val drawable: Drawable? = if (this@ChatBubbleAdapter::avatar.isInitialized) {
                        avatar
                    } else {
                        Drawable.createFromPath(avFilename)
                    }

                    // file not exist - maybe user cleared cache? it will be updated later then
                    if (drawable != null) {
                        binding.avatar.background = drawable
                    }
                }
            } else if (item.message.chat_type == ChatType.GROUP) {
                binding.chatGroupUsername.apply {
                    text = item.message.group_chat_username
                    visibility = VISIBLE
                }

                // TODO implement some kind of special handling for group chat avatars based off of username
            }
        }
    }

    inner class ChatBubbleSenderViewHolder(private val binding: ItemChatBubbleSenderBinding) : ChatBubbleViewHolder<MessageWithAvatar>(binding) {
        override fun bind(item: MessageWithAvatar, position: Int) {
            binding.chatBubbleSender.text = item.message.message
        }
    }

    inner class ChatBubbleRecalledVisibleViewHolder(private val binding: ItemBubbleRecalledBinding) : ChatBubbleViewHolder<MessageWithAvatar>(binding) {
        override fun bind(item: MessageWithAvatar, position: Int) {
            binding.chatBubbleRecalled.text = context.getString(R.string.message_header_recalled_visible)
                .replace("%s", if (item.message.chat_type == ChatType.CHAT) activity.mChatSelectedTitle else item.message.group_chat_username)
                .replace("%m", item.message.message)
        }
    }

    inner class ChatBubbleRecalledHiddenViewHolder(private val binding: ItemBubbleRecalledBinding) : ChatBubbleViewHolder<MessageWithAvatar>(binding) {
        override fun bind(item: MessageWithAvatar, position: Int) {
            binding.chatBubbleRecalled.text = context.getString(R.string.message_header_recalled_invisible)
                .replace("%s", if (item.message.chat_type == ChatType.CHAT) activity.mChatSelectedTitle else item.message.group_chat_username)
        }
    }

    inner class ChatBubbleDateHeaderViewHolder(private val binding: ItemBubbleDateHeaderBinding) : ChatBubbleViewHolder<MessageWithAvatar>(binding) {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun bind(item: MessageWithAvatar, position: Int) {
            binding.chatBubbleDateHeader.text = DateConverter.toDateMessage(context, item.message.timestamp)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatBubbleViewHolder<MessageWithAvatar> {
        val layoutInflater = LayoutInflater.from(parent.context)

        return when (viewType) {
            MessageType.RECEIVER.ordinal -> {
                val binding = ItemChatBubbleReceiverBinding.inflate(layoutInflater, parent, false)
                ChatBubbleReceiverViewHolder(binding)
            }
            MessageType.SENDER.ordinal -> {
                val binding = ItemChatBubbleSenderBinding.inflate(layoutInflater, parent, false)
                binding.selfUserData = selfUserData
                ChatBubbleSenderViewHolder(binding)
            }
            MessageType.RECALLED_VISIBLE.ordinal -> {
                val binding = ItemBubbleRecalledBinding.inflate(layoutInflater, parent, false)
                ChatBubbleRecalledVisibleViewHolder(binding)
            }
            MessageType.RECALLED_HIDDEN.ordinal -> {
                val binding = ItemBubbleRecalledBinding.inflate(layoutInflater, parent, false)
                ChatBubbleRecalledHiddenViewHolder(binding)
            }
            MessageType.DATE_HEADER.ordinal -> {
                val binding = ItemBubbleDateHeaderBinding.inflate(layoutInflater, parent, false)
                ChatBubbleDateHeaderViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: ChatBubbleViewHolder<MessageWithAvatar>, position: Int) {
        val element = getItem(position)!!
        when (holder) {
            is ChatBubbleReceiverViewHolder -> holder.bind(element, position)
            is ChatBubbleSenderViewHolder -> holder.bind(element, position)
            is ChatBubbleRecalledVisibleViewHolder -> holder.bind(element, position)
            is ChatBubbleRecalledHiddenViewHolder -> holder.bind(element, position)
            is ChatBubbleDateHeaderViewHolder -> holder.bind(element, position)
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)?.message?.message_type) {
            MessageType.SENDER -> MessageType.SENDER.ordinal
            MessageType.RECEIVER -> MessageType.RECEIVER.ordinal
            MessageType.RECALLED_VISIBLE -> MessageType.RECALLED_VISIBLE.ordinal
            MessageType.RECALLED_HIDDEN -> MessageType.RECALLED_HIDDEN.ordinal
            MessageType.DATE_HEADER -> MessageType.DATE_HEADER.ordinal
            null -> throw IllegalArgumentException("Invalid item type")
        }
    }
}
