package com.oasisfeng.nevo.decorators.wechat.chatHistory.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.oasisfeng.nevo.decorators.wechat.EmojiTranslator
import com.oasisfeng.nevo.decorators.wechat.R
import com.oasisfeng.nevo.decorators.wechat.chatHistory.DateConverter
import com.oasisfeng.nevo.decorators.wechat.chatHistory.Helpers
import com.oasisfeng.nevo.decorators.wechat.chatHistory.activity.ChatHistoryFragmentActivity
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.UserWithMessageAndAvatar
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.type.ChatType
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.type.MessageType
import com.oasisfeng.nevo.decorators.wechat.databinding.ItemUserBinding
import java.util.*

interface UserAdapterOnClickListener {
    fun userOnClick(uid: Long, username: String)
    fun userOnLongClick(uid: Long, username: String): Boolean
}

class UserAdapter(
    private val context: Context,
    private val callbacks: UserAdapterOnClickListener,
    private val adapterDataList: List<UserWithMessageAndAvatar>
) : RecyclerView.Adapter<UserAdapter.UserItemViewHolder>() {
    val activity = context as ChatHistoryFragmentActivity

    inner class UserItemViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserItemViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemUserBinding.inflate(layoutInflater, parent, false)
        return UserItemViewHolder(binding)
    }

    override fun getItemCount(): Int {
        return adapterDataList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: UserItemViewHolder, position: Int) {
        val data = adapterDataList[position]

        val dateString = DateConverter.toDateUserlist(context, data.user.latest_message)

        val filename = data.data.avatar.filename
        var userAvatar: Drawable? = null
        if (filename.isNotEmpty()) {
            userAvatar = Drawable.createFromPath(data.data.avatar.filename)
        }

        holder.binding.apply {
            username.text = EmojiTranslator.translate(
                data.user.username,
                context,
                Helpers.sizeToEmojiHeight(username.textSize),
                true,
                true
            )
            avatar.background = userAvatar ?: ResourcesCompat.getDrawable(context.resources, R.drawable.blank_user, null)
        }

        holder.binding.message.text = if (activity.mSharedViewModel.drafts.contains(data.user.u_id)) {
            val ssb = SpannableStringBuilder(context.getString(R.string.drafts))

            val color = ForegroundColorSpan(context.getColor(R.color.draft_label))
            ssb.setSpan(color, 0, ssb.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)

            ssb.append(EmojiTranslator.translate(
                activity.mSharedViewModel.drafts[data.user.u_id]!!,
                context,
                Helpers.sizeToEmojiHeight(holder.binding.message.textSize),
                true
            ))
            ssb
        } else {
            if (data.data.message.message_type == MessageType.RECALLED_HIDDEN || data.data.message.message_type == MessageType.RECALLED_VISIBLE) {
                val title = if (data.data.message.chat_type == ChatType.CHAT) {
                    data.user.username
                } else {
                    data.data.message.group_chat_username
                }

                val size = Helpers.sizeToEmojiHeight(holder.binding.message.textSize)

                val titleTrans = EmojiTranslator.translate(
                    title,
                    context,
                    size,
                    true,
                    true
                )

                if (data.data.message.message_type == MessageType.RECALLED_VISIBLE) {
                    val msgTrans = EmojiTranslator.translate(
                        data.data.message.message,
                        context,
                        size,
                        true
                    )

                    val spannable = SpannableStringBuilder(
                        context.getString(R.string.message_header_recalled_visible)
                    )
                    spannable.replace(1, 3, titleTrans)
                    spannable.replace(spannable.length - 2, spannable.length, msgTrans)

                    spannable
                } else {
                    val spannable = SpannableStringBuilder(
                        context.getString(R.string.message_header_recalled_invisible)
                    )

                    spannable.replace(1, 3, titleTrans)

                    spannable
                }
            } else {
                EmojiTranslator.translate(
                    data.data.message.message,
                    context,
                    Helpers.sizeToEmojiHeight(holder.binding.message.textSize),
                    true
                )
            }
        }

        holder.binding.date.text = dateString

        holder.itemView.apply {
            setOnClickListener {
                callbacks.userOnClick(data.user.u_id, data.user.username)
            }

            setOnLongClickListener {
                return@setOnLongClickListener callbacks.userOnLongClick(
                    data.user.u_id,
                    data.user.username
                )
            }
        }
    }
}
