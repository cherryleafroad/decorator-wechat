package com.oasisfeng.nevo.decorators.wechat.chatHistory.adapter

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import androidx.recyclerview.widget.RecyclerView
import com.oasisfeng.nevo.decorators.wechat.R
import com.oasisfeng.nevo.decorators.wechat.chatHistory.DateConverter
import com.oasisfeng.nevo.decorators.wechat.chatHistory.database.entity.UserWithMessageAndAvatar
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
            username.text = data.user.username
            message.text = data.data.message.message
            date.text = dateString
            avatar.background = userAvatar ?: ResourcesCompat.getDrawable(context.resources, R.drawable.blank_user, null)
        }

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
