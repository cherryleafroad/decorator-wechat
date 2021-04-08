package com.oasisfeng.nevo.decorators.wechat.chatHistory

object Helpers {
    fun sizeToEmojiHeight(size: Float): Int {
        val num = size + (size * .3)
        return num.toInt()
    }
}