package com.oasisfeng.nevo.decorators.wechat.chatui

import android.content.Context
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.oasisfeng.nevo.decorators.wechat.R

class MessageBubbles {
    companion object {
        private const val TEXTSIZE = 17F
        private const val DP_MARGIN = 12
        private const val BUBBLE_BOTTOM_MARGIN_DP = 7
        private const val BUBBLE_RIGHT_MARGIN_DP = 40
        private const val BUBBLE_LEFT_MARGIN_DP = 40

        private enum class BubbleType {
            Sender, Receiver
        }

        fun createSenderBubble(context: Context, text: String): RelativeLayout {
            return generateChatBubble(context, BubbleType.Sender, R.drawable.chat_sender_bubble, R.color.chat_sender_bubble_text, text)
        }

        fun createReceiverBubble(context: Context, text: String): RelativeLayout {
            return generateChatBubble(context, BubbleType.Receiver, R.drawable.chat_receiver_bubble, R.color.chat_receiver_bubble_text, text)
        }

        private fun generateChatBubble(context: Context, type: BubbleType, bubble: Int, textColor: Int, text: String): RelativeLayout {
            val layout = createLayout(context, type)

            val chatBubble = ContextCompat.getDrawable(context, bubble)
            layout.background = chatBubble

            val textview = createTextView(context)
            textview.setTextColor(context.getColor(textColor))

            textview.setText(text, TextView.BufferType.NORMAL)

            layout.addView(textview)
            return layout
        }

        private fun createLayout(context: Context, type: BubbleType): RelativeLayout {
            val layout = RelativeLayout(context)
            layout.gravity = Gravity.CENTER
            val id = View.generateViewId()
            layout.id = id
            // android:background drawable
            layout.visibility = View.VISIBLE

            val layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            val marginParams = ViewGroup.MarginLayoutParams(layoutParams)
            val bottom = pxToDp(context, BUBBLE_BOTTOM_MARGIN_DP)

            if (type == BubbleType.Receiver) {
                val right = pxToDp(context, BUBBLE_RIGHT_MARGIN_DP)
                marginParams.setMargins(0, 0, right, bottom)
            } else {
                val left = pxToDp(context, BUBBLE_LEFT_MARGIN_DP)
                marginParams.setMargins(left, 0, 0, bottom)
            }

            layout.layoutParams = marginParams

            return layout
        }

        private fun createTextView(context: Context): TextView {
            val textview = TextView(context)
            textview.setTextSize(TypedValue.COMPLEX_UNIT_SP, TEXTSIZE)
            val id = View.generateViewId()
            textview.id = id

            val layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )

            val marginParams = ViewGroup.MarginLayoutParams(layoutParams)

            val dp = pxToDp(context, DP_MARGIN)
            marginParams.setMargins(dp, dp, dp, dp)

            textview.layoutParams = marginParams
            return textview
        }

        private fun pxToDp(context: Context, dp: Int): Int {
            val scale: Float = context.resources.displayMetrics.density
            return (dp * scale + 0.5f).toInt()
        }
    }
}