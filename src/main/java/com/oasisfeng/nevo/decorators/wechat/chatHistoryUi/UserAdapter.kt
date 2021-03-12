package com.oasisfeng.nevo.decorators.wechat.chatHistoryUi

import android.content.Context
import android.graphics.drawable.Drawable
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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

    override fun onBindViewHolder(holder: UserItemViewHolder, position: Int) {
        val username = holder.itemView.findViewById<TextView>(R.id.username)
        val data = adapterDataList[position]
        username.text = data.user.username

        val message = holder.itemView.findViewById<TextView>(R.id.message)
        message.text = data.data.message.message

        val dateText = holder.itemView.findViewById<TextView>(R.id.date)
        val date = Date(data.data.message.timestamp)
        val dateFmt = DateFormat.getDateFormat(context).format(date)
        dateText.text = dateFmt

        val avatar = Drawable.createFromPath(data.data.avatar.filename)
        if (avatar != null) {
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
