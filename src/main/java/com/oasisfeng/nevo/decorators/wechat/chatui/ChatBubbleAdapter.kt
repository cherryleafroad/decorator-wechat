package com.oasisfeng.nevo.decorators.wechat.chatui

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.oasisfeng.nevo.decorators.wechat.R


class ChatBubbleAdapter(
    private val context: Context,
    private val adapterDataList: List<Any>
) : RecyclerView.Adapter<ChatBubbleAdapter.ChatBubbleViewHolder<*>>() {
    companion object {
        private const val TYPE_BUBBLE_RECEIVER = 0
        private const val TYPE_BUBBLE_SENDER = 1
    }

    abstract class ChatBubbleViewHolder<T>(itemView: View) : RecyclerView.ViewHolder(itemView) {
        abstract fun bind(item: T)
    }

    inner class ChatBubbleReceiverViewHolder(itemView: View) : ChatBubbleViewHolder<ChatBubbleReceiver>(
        itemView
    ) {
        override fun bind(item: ChatBubbleReceiver) {
            val layout = (this.itemView as ConstraintLayout)
            val textview = layout.getViewById(R.id.chat_bubble_receiver) as TextView
            textview.text = item.message
        }
    }

    inner class ChatBubbleSenderViewHolder(itemView: View) : ChatBubbleViewHolder<ChatBubbleSender>(
        itemView
    ) {
        override fun bind(item: ChatBubbleSender) {
            val constraint = ((this.itemView as FrameLayout).getChildAt(0) as ConstraintLayout)
            val textview = constraint.getViewById(R.id.chat_bubble_sender) as TextView
            textview.text = item.message
        }
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatBubbleViewHolder<*> {
        return when (viewType) {
            TYPE_BUBBLE_RECEIVER -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_chat_bubble_receiver, parent, false)
                ChatBubbleReceiverViewHolder(view)
            }
            TYPE_BUBBLE_SENDER -> {
                val view = LayoutInflater.from(context)
                    .inflate(R.layout.item_chat_bubble_sender, parent, false)
                ChatBubbleSenderViewHolder(view)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: ChatBubbleViewHolder<*>, position: Int) {
        val element = adapterDataList[position]
        when (holder) {
            is ChatBubbleReceiverViewHolder -> holder.bind(element as ChatBubbleReceiver)
            is ChatBubbleSenderViewHolder -> holder.bind(element as ChatBubbleSender)
            else -> throw IllegalArgumentException()
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (adapterDataList[position]) {
            is ChatBubbleReceiver -> TYPE_BUBBLE_RECEIVER
            is ChatBubbleSender -> TYPE_BUBBLE_SENDER
            else -> throw IllegalArgumentException("Invalid type of data $position")
        }
    }

    override fun getItemCount(): Int {
        return adapterDataList.size
    }
}
