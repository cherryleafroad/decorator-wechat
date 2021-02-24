package com.oasisfeng.nevo.decorators.wechat;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class WeChatApp extends Application {

    private SharedPreferences preferences;

    public SharedPreferences getSharedPreferences() { return preferences; }

    public void setSharedPreferences(SharedPreferences pref) { this.preferences = pref; }
}
