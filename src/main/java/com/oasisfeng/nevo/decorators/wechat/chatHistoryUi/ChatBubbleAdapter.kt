package com.oasisfeng.nevo.decorators.wechat.chatHistoryUi

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.util.ArrayMap
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.oasisfeng.nevo.decorators.wechat.R


class ChatBubbleAdapter(
    private val context: Context
) : PagingDataAdapter<MessageWithAvatar, ChatBubbleAdapter.ChatBubbleViewHolder<MessageWithAvatar>>(DIFF_CALLBACK) {

    private lateinit var avatar: Drawable
    private lateinit var groupAvatars: ArrayMap<String, Drawable>

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<MessageWithAvatar>() {
            override fun areContentsTheSame(oldItem: MessageWithAvatar, newItem: MessageWithAvatar): Boolean {
                return oldItem.message.timestamp == newItem.message.timestamp
            }

            override fun areItemsTheSame(oldItem: MessageWithAvatar, newItem: MessageWithAvatar): Boolean {
                return oldItem.message == newItem.message
            }
        }
    }

    abstract class ChatBubbleViewHolder<MessageWithAvatar>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: MessageWithAvatar, position: Int)
    }

    inner class ChatBubbleReceiverViewHolder(itemView: View) : ChatBubbleViewHolder<MessageWithAvatar>(itemView) {
        override fun bind(item: MessageWithAvatar, position: Int) {
            val layout = (this.itemView as ConstraintLayout)
            val textview = layout.findViewById<TextView>(R.id.chat_bubble_receiver)
            textview.text = item.message.message

            val avFilename = item.avatar.filename
            if (item.message.chat_type == ChatType.CHAT) {
                if (avFilename.isNotEmpty()) {
                    val imageview = layout.findViewById<ImageView>(R.id.avatar)
                    val drawable: Drawable? = if (this@ChatBubbleAdapter::avatar.isInitialized) {
                        avatar
                    } else {
                        Drawable.createFromPath(avFilename)
                    }

                    // file not exist - maybe user cleared cache? it will be updated later then
                    if (drawable != null) {
                        imageview.background = drawable
                    }
                }
            } else if (item.message.chat_type == ChatType.GROUP) {
                val groupusername = layout.findViewById<TextView>(R.id.chat_group_username)
                groupusername.text = item.message.group_chat_username
                groupusername.visibility = VISIBLE

                // TODO implement some kind of special handling for group chat avatars based off of username
            }
        }
    }

    inner class ChatBubbleSenderViewHolder(itemView: View) : ChatBubbleViewHolder<MessageWithAvatar>(itemView) {
        override fun bind(item: MessageWithAvatar, position: Int) {
            val constraint = this.itemView as ConstraintLayout
            val textview = constraint.findViewById<TextView>(R.id.chat_bubble_sender)
            //val imageview = constraint.getViewById(R.id.avatar) as ImageView
            textview.text = item.message.message
        }
    }

    inner class ChatBubbleRecalledViewHolder(itemView: View) : ChatBubbleViewHolder<MessageWithAvatar>(itemView) {
        override fun bind(item: MessageWithAvatar, position: Int) {
            val constraint = ((this.itemView as FrameLayout).getChildAt(0) as ConstraintLayout)
            val textview = constraint.getViewById(R.id.chat_bubble_recalled) as TextView
            textview.text = item.message.message
        }
    }

    inner class ChatBubbleDateHeaderViewHolder(itemView: View) : ChatBubbleViewHolder<MessageWithAvatar>(itemView) {
        @RequiresApi(Build.VERSION_CODES.N)
        override fun bind(item: MessageWithAvatar, position: Int) {
            val constraint = ((this.itemView as FrameLayout).getChildAt(0) as ConstraintLayout)
            val textview = constraint.getViewById(R.id.chat_bubble_date_header) as TextView
            textview.text = DateConverter.toDateMessage(context, item.message.timestamp)
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatBubbleViewHolder<MessageWithAvatar> {
        return when (viewType) {
            MessageType.RECEIVER.ordinal -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_chat_bubble_receiver, parent, false)
                ChatBubbleReceiverViewHolder(view)
            }
            MessageType.SENDER.ordinal -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_chat_bubble_sender, parent, false)
                ChatBubbleSenderViewHolder(view)
            }
            MessageType.RECALLED.ordinal -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_bubble_recalled, parent, false)
                ChatBubbleRecalledViewHolder(view)
            }
            MessageType.DATE_HEADER.ordinal -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_bubble_date_header, parent, false)
                ChatBubbleDateHeaderViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: ChatBubbleViewHolder<MessageWithAvatar>, position: Int) {
        val element = getItem(position)!!
        when (holder) {
            is ChatBubbleReceiverViewHolder -> holder.bind(element, position)
            is ChatBubbleSenderViewHolder -> holder.bind(element, position)
            is ChatBubbleRecalledViewHolder -> holder.bind(element, position)
            is ChatBubbleDateHeaderViewHolder -> holder.bind(element, position)
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)?.message?.message_type) {
            MessageType.SENDER -> MessageType.SENDER.ordinal
            MessageType.RECEIVER -> MessageType.RECEIVER.ordinal
            MessageType.RECALLED -> MessageType.RECALLED.ordinal
            MessageType.DATE_HEADER -> MessageType.DATE_HEADER.ordinal
            null -> throw IllegalArgumentException("Invalid item type")
        }
    }
}
