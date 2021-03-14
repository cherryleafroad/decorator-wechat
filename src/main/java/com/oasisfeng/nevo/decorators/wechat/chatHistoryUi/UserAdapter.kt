package com.oasisfeng.nevo.decorators.wechat.chatHistoryUi

import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Build
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import com.oasisfeng.nevo.decorators.wechat.R
import java.util.*

class UserAdapter(
        private val context: Context,
        private val adapterDataList: List<UserWithMessageAndAvatar>
) : RecyclerView.Adapter<UserAdapter.UserItemViewHolder>() {

    inner class UserItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserItemViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_user, parent, false)
        return UserItemViewHolder(view)
    }

    override fun getItemCount(): Int {
        return adapterDataList.size
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: UserItemViewHolder, position: Int) {
        val username = holder.itemView.findViewById<TextView>(R.id.username)
        val data = adapterDataList[position]
        username.text = data.user.username

        val message = holder.itemView.findViewById<TextView>(R.id.message)
        message.text = data.data.message.message

        val dateText = holder.itemView.findViewById<TextView>(R.id.date)
        val dateString = DateConverter.toDateUserlist(context, data.user.latest_message)
        dateText.text = dateString

        val filename = data.data.avatar.filename
        if (filename.isNotEmpty()) {
            val avatar = Drawable.createFromPath(data.data.avatar.filename)
            val imageview = holder.itemView.findViewById<ImageView>(R.id.avatar)
            imageview.background = avatar
        }

        holder.itemView.setOnClickListener {
            (context as UserActivity).userOnClick(data.user.u_id, data.user.username)
        }
        holder.itemView.setOnLongClickListener {
            return@setOnLongClickListener (context as UserActivity).userLongOnClick(data.user.u_id, data.user.username)
        }
    }
}
