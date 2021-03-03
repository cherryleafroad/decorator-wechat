package com.oasisfeng.nevo.decorators.wechat

import android.app.Application
import android.content.SharedPreferences
import android.util.ArrayMap

class WeChatApp : Application() {
    @JvmField
    var whenMap: ArrayMap<String, Long> = ArrayMap()
    var replying = false
    var lastIsRecalled = false
    var isRecasted = false
    var sharedPreferences: SharedPreferences? = null
    var appLaunched = false
    var appRemovedNotif = false
}