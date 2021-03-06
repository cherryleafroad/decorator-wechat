package com.oasisfeng.nevo.decorators.wechat.chatHistoryUi

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.oasisfeng.nevo.decorators.wechat.R
import com.oasisfeng.nevo.decorators.wechat.chatHistoryUi.ChatHistoryActivity.Companion.EXTRA_USERNAME
import com.oasisfeng.nevo.decorators.wechat.chatHistoryUi.ChatHistoryActivity.Companion.EXTRA_USER_ID

class UserAdapter(
        private val context: Context,
        private val adapterDataList: List<UserItem>
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
        val textview = holder.itemView.findViewById(R.id.user_item) as TextView
        val data = adapterDataList[position]
        textview.text = data.username

        textview.setOnClickListener {
            (context as UserActivity).userOnClick(data.user_id, data.username)
        }
    }
}
