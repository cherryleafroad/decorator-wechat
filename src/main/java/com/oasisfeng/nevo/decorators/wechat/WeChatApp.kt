package com.oasisfeng.nevo.decorators.wechat

import android.app.Application
import android.content.SharedPreferences

class WeChatApp : Application() {
    var replying = false
    var lastIsRecalled = false
    var isRecasted = false
    var sharedPreferences: SharedPreferences? = null
}