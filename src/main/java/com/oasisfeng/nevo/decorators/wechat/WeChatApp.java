package com.oasisfeng.nevo.decorators.wechat;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

public class WeChatApp extends Application {

    private boolean isReplying = false;
    private SharedPreferences preferences;

    public boolean getReplying() {
        return isReplying;
    }
    public SharedPreferences getSharedPreferences() { return preferences; }

    public void setReplying(boolean state) {
        this.isReplying = state;
    }
    public void setSharedPreferences(SharedPreferences pref) { this.preferences = pref; }
}
