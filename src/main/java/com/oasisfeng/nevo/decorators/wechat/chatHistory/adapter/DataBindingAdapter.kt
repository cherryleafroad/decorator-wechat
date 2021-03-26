package com.oasisfeng.nevo.decorators.wechat.chatHistory.adapter

import android.view.View
import android.view.ViewGroup
import androidx.databinding.BindingAdapter

class DataBindingAdapter {
    companion object {
        @JvmStatic
        @BindingAdapter("android:layout_marginEnd")
        fun setLayoutMarginEnd(view: View, dimen: Float) {
            val layoutParams = view.layoutParams as ViewGroup.MarginLayoutParams
            layoutParams.marginEnd = dimen.toInt()
            view.layoutParams = layoutParams
        }
    }
}
